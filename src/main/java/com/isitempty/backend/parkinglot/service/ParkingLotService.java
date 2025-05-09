package com.isitempty.backend.parkinglot.service;

import com.isitempty.backend.parkinglot.dto.response.ParkingLotRes;
import com.isitempty.backend.parkinglot.entity.ParkingLot;
import com.isitempty.backend.parkinglot.repository.ParkingLotRepository;
import com.isitempty.backend.utils.GeoUtils;
import com.isitempty.backend.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ParkingLotService {

    private final ParkingLotRepository parkingLotRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    
    // @Lazy 어노테이션을 사용하여 순환 참조 문제 해결
    private final ParkingLotMappingService mappingService;
    private final ReviewService reviewService;
    
    private static final String REALTIME_CACHE_KEY = "parking:realtime:";
    private static final String ADDRESS_CACHE_KEY = "parking:address:";
    private static final String PHONE_CACHE_KEY = "parking:phone:";

    @Autowired
    public ParkingLotService(ParkingLotRepository parkingLotRepository,
                           RedisTemplate<String, Object> redisTemplate,
                           @Lazy ParkingLotMappingService mappingService,
                           ReviewService reviewService) {
        this.parkingLotRepository = parkingLotRepository;
        this.redisTemplate = redisTemplate;
        this.mappingService = mappingService;
        this.reviewService = reviewService;
    }

    // 전체 주차장 리스트 조회
    public List<ParkingLot> getAllParkingLots() {
        return parkingLotRepository.findAll();
    }

    // 특정 주차장 정보 조회
    public ParkingLot getParkingLotById(String id) {
        return parkingLotRepository.findById(id)
                .orElse(null);
    }

    // 주소 정규화
    public String normalizeAddress(String address) {
        return com.isitempty.backend.utils.ParkingLotUtils.normalizeAddress(address);
    }

    // 전화번호 정규화
    public String normalizePhoneNumber(String phone) {
        return com.isitempty.backend.utils.ParkingLotUtils.normalizePhoneNumber(phone);
    }

    // 실시간 여석 통합 주차장 리스트
    public List<ParkingLotRes> getNearbyParkingLots(double latitude, double longitude) {
        // 반경을 2.0km로 줄여 검색 속도 향상
        float radius = 2.0f;
        List<ParkingLotRes> result = new ArrayList<>();
        
        try {
            log.info("[성능측정] 주변 주차장 검색 시작: 위도={}, 경도={}, 반경={}km", latitude, longitude, radius);
            
            List<Object[]> parkingLotData = parkingLotRepository.findNearbyParkingLots(latitude, longitude, radius);
            log.info("[성능측정] 주변 주차장 검색 결과: {} 건", parkingLotData.size());
            
            // 결과가 없으면 로그 추가
            if (parkingLotData.isEmpty()) {
                log.warn("주변 주차장 검색 쿼리 결과가 없습니다. 좌표: ({}, {}), 반경: {}km", 
                         latitude, longitude, radius);
                return result;
            }
            
            // 정적 ID 목록 추출
            List<String> staticIds = parkingLotData.stream()
                .map(data -> (String) data[0])
                .filter(id -> id != null)
                .collect(Collectors.toList());
            
            long start = System.currentTimeMillis();
            
            // 주차장 상세 정보를 ID별로 미리 조회하여 캐싱
            Map<String, ParkingLot> parkingLotDetails = parkingLotRepository.findAllById(staticIds)
                .stream()
                .collect(Collectors.toMap(ParkingLot::getId, lot -> lot, (old, newVal) -> newVal));
            log.info("[성능측정] 주차장 상세 정보 일괄 조회: {}개, 소요시간: {}ms", 
                    parkingLotDetails.size(), System.currentTimeMillis() - start);
            
            // 매핑 정보 일괄 조회
            start = System.currentTimeMillis();
            final Map<String, String> mappingsById = new HashMap<>();
            if (mappingService != null) {
                mappingsById.putAll(mappingService.getRealtimeIdsByStaticIds(staticIds));
                log.info("[성능측정] 매핑 정보 일괄 조회 결과: {} 건, 소요시간: {}ms", 
                        mappingsById.size(), System.currentTimeMillis() - start);
            } else {
                log.warn("MappingService is not yet initialized, skipping ID mapping lookup");
            }
            
            // Redis에서 여석 정보 일괄 조회를 위한 키 목록 준비
            start = System.currentTimeMillis();
            List<String> redisKeys = new ArrayList<>();
            
            // 1. 매핑 ID 기반 키 추가
            for (String realtimeId : mappingsById.values()) {
                if (realtimeId != null && !realtimeId.isEmpty()) {
                    redisKeys.add(REALTIME_CACHE_KEY + realtimeId);
                }
            }
            
            // 2. 주소/전화번호 기반 키 추가 - 일괄 처리 최적화
            Map<String, String> normalizedAddresses = new HashMap<>();
            Map<String, String> normalizedPhones = new HashMap<>();
            
            for (ParkingLot lot : parkingLotDetails.values()) {
                // 주소 정규화 및 캐싱
                if (lot.getLotAddress() != null && !lot.getLotAddress().isEmpty()) {
                    String normalized = normalizeAddress(lot.getLotAddress());
                    if (!normalized.isEmpty()) {
                        normalizedAddresses.put(lot.getId(), normalized);
                        redisKeys.add(ADDRESS_CACHE_KEY + normalized);
                    }
                }
                
                // 전화번호 정규화 및 캐싱
                if (lot.getPhone() != null && !lot.getPhone().isEmpty()) {
                    String normalized = normalizePhoneNumber(lot.getPhone());
                    if (!normalized.isEmpty()) {
                        normalizedPhones.put(lot.getId(), normalized);
                        redisKeys.add(PHONE_CACHE_KEY + normalized);
                    }
                }
            }
            
            // Redis에서 여석 정보 일괄 조회
            Map<String, Integer> availableSpotsByKey = new HashMap<>();
            if (!redisKeys.isEmpty()) {
                List<Object> redisValues = redisTemplate.opsForValue().multiGet(redisKeys);
                if (redisValues != null) {
                    for (int i = 0; i < redisKeys.size(); i++) {
                        if (redisValues.get(i) != null) {
                            try {
                                // 값 파싱 시도
                                Object value = redisValues.get(i);
                                Integer spots = parseRedisValue(value);
                                if (spots != null) {
                                    availableSpotsByKey.put(redisKeys.get(i), spots);
                                }
                            } catch (Exception e) {
                                log.warn("Redis 값 파싱 중 오류: {}", e.getMessage());
                            }
                        }
                    }
                }
            }
            log.info("[성능측정] Redis 여석 정보 일괄 조회: {}개 키 중 {}개 조회됨, 소요시간: {}ms", 
                    redisKeys.size(), availableSpotsByKey.size(), System.currentTimeMillis() - start);
            
            start = System.currentTimeMillis();
            
            // 주차장 데이터 처리 병렬화하여 속도 개선
            result = parkingLotData.parallelStream().map(data -> {
                try {
                    if (data == null || data.length < 5) {
                        log.warn("유효하지 않은 주차장 데이터: {}", data);
                        return null;
                    }
                    
                    String staticId = (String) data[0];
                    String name = (String) data[1];
                    
                    // 숫자 타입 안전하게 변환
                    Double lat = null;
                    if (data[2] != null) {
                        if (data[2] instanceof Number) {
                            lat = ((Number)data[2]).doubleValue();
                        } else {
                            lat = Double.parseDouble(data[2].toString());
                        }
                    }
                    
                    Double lng = null;
                    if (data[3] != null) {
                        if (data[3] instanceof Number) {
                            lng = ((Number)data[3]).doubleValue();
                        } else {
                            lng = Double.parseDouble(data[3].toString());
                        }
                    }
                    
                    String address = (String) data[4];
                    
                    // 필수 데이터 검증
                    if (staticId == null || name == null || lat == null || lng == null) {
                        log.warn("주차장 필수 데이터 누락: ID={}, 이름={}, 위도={}, 경도={}", 
                                staticId, name, lat, lng);
                        return null;
                    }
                    
                    // 거리 정보 계산
                    Double distance = null;
                    if (data.length > 5 && data[5] != null) {
                        if (data[5] instanceof Number) {
                            distance = ((Number)data[5]).doubleValue();
                        } else {
                            try {
                                distance = Double.parseDouble(data[5].toString());
                            } catch (NumberFormatException e) {
                                log.warn("거리 정보 파싱 오류: {}", e.getMessage());
                            }
                        }
                    }
                    
                    // 캐싱된 상세 정보에서 가져오기
                    ParkingLot fullLotInfo = parkingLotDetails.get(staticId);
                    int slotCount = 0;
                    if (fullLotInfo != null && fullLotInfo.getSlotCount() != null) {
                        slotCount = fullLotInfo.getSlotCount();
                    }
                    
                    // 여석 정보 조회 - 미리 조회한 매핑 정보 활용
                    Integer availableSpots = null;
                    
                    // 1. 매핑된 실시간 ID로 조회
                    String realtimeId = mappingsById.get(staticId);
                    if (realtimeId != null) {
                        String redisKey = REALTIME_CACHE_KEY + realtimeId;
                        availableSpots = availableSpotsByKey.get(redisKey);
                    }
                    
                    // 2. 주소 기반 조회
                    if (availableSpots == null && normalizedAddresses.containsKey(staticId)) {
                        String normalized = normalizedAddresses.get(staticId);
                        String redisKey = ADDRESS_CACHE_KEY + normalized;
                        availableSpots = availableSpotsByKey.get(redisKey);
                        
                        if (availableSpots != null) {
                            // 성공 매핑 업데이트 (비동기로 처리)
                            String extractedRealtimeId = extractIdFromRedisKey(redisKey);
                            if (extractedRealtimeId != null && mappingService != null) {
                                // 여석 정보 처리 속도에 영향을 주지 않도록 별도 스레드에서 처리
                                new Thread(() -> {
                                    mappingService.updateMappingFromSuccessfulMatch(staticId, extractedRealtimeId);
                                }).start();
                            }
                        }
                    }
                    
                    // 3. 전화번호 기반 조회
                    if (availableSpots == null && normalizedPhones.containsKey(staticId)) {
                        String normalized = normalizedPhones.get(staticId);
                        String redisKey = PHONE_CACHE_KEY + normalized;
                        availableSpots = availableSpotsByKey.get(redisKey);
                        
                        if (availableSpots != null) {
                            // 성공 매핑 업데이트 (비동기로 처리)
                            String extractedRealtimeId = extractIdFromRedisKey(redisKey);
                            if (extractedRealtimeId != null && mappingService != null) {
                                // 여석 정보 처리 속도에 영향을 주지 않도록 별도 스레드에서 처리
                                new Thread(() -> {
                                    mappingService.updateMappingFromSuccessfulMatch(staticId, extractedRealtimeId);
                                }).start();
                            }
                        }
                    }
                    
                    // 평균 리뷰 점수 조회
                    Double averageRating = reviewService.getAverageRatingByParkingLotId(staticId);
                    
                    // 최종 결과 생성 - Builder 패턴 사용
                    ParkingLotRes.ParkingLotResBuilder builder = ParkingLotRes.builder()
                        .id(staticId)
                        .name(name)
                        .address(address)
                        .latitude(lat)
                        .longitude(lng)
                        .slotCount(slotCount)
                        .availableSpots(availableSpots)
                        .distance(distance)
                        .averageRating(averageRating);
                    
                    // 상세 정보 추가 (null 체크)
                    if (fullLotInfo != null) {
                        builder.phone(fullLotInfo.getPhone())
                              .type(fullLotInfo.getType())
                              .category(fullLotInfo.getCategory())
                              .roadAddress(fullLotInfo.getRoadAddress())
                              .zoneType(fullLotInfo.getZoneType())
                              .rotationType(fullLotInfo.getRotationType())
                              .openDays(fullLotInfo.getOpenDays())
                              .feeInfo(fullLotInfo.getFeeInfo())
                              .baseFee(fullLotInfo.getBaseFee())
                              .baseTime(fullLotInfo.getBaseTime())
                              .addFee(fullLotInfo.getAddFee())
                              .addTime(fullLotInfo.getAddTime())
                              .dayTicketFee(fullLotInfo.getDayTicketFee())
                              .monthTicketFee(fullLotInfo.getMonthTicketFee())
                              .paymentMethod(fullLotInfo.getPaymentMethod())
                              .remarks(fullLotInfo.getRemarks())
                              .adminName(fullLotInfo.getAdminName());
                    }
                    
                    return builder.build();
                } catch (Exception e) {
                    log.error("주차장 데이터 변환 중 오류 발생: {}", e.getMessage(), e);
                    return null;
                }
            })
            .filter(res -> res != null)
            .collect(Collectors.toList());
            
            log.info("[성능측정] 주차장 데이터 변환 처리: {}개, 소요시간: {}ms", 
                    result.size(), System.currentTimeMillis() - start);
            
            log.info("주변 주차장 검색 완료: 총 {}건", result.size());
            
        } catch (Exception e) {
            log.error("주변 주차장 검색 중 오류 발생: {}", e.getMessage(), e);
        }
        
        return result;
    }
    
    /**
     * Redis 값을 Integer로 파싱
     */
    private Integer parseRedisValue(Object redisValue) {
        if (redisValue == null) {
            return null;
        }
        
        if (redisValue instanceof Integer i) {
            return i;
        } else if (redisValue instanceof Long l) {
            return l.intValue();
        } else if (redisValue instanceof Double d) {
            return d.intValue();
        } else if (redisValue instanceof String s) {
            if (s.matches("\\d+")) {
                return Integer.parseInt(s);
            }
        } else if (redisValue instanceof Map) {
            try {
                Map<?, ?> map = (Map<?, ?>)redisValue;
                if (map.containsKey("availableSpots")) {
                    Object spots = map.get("availableSpots");
                    if (spots instanceof Number) {
                        return ((Number)spots).intValue();
                    } else if (spots instanceof String && ((String)spots).matches("\\d+")) {
                        return Integer.parseInt((String)spots);
                    }
                } else if (map.containsKey("totalSpaces") && map.containsKey("currentParked")) {
                    // RealtimeRes 또는 유사한 구조 처리
                    Object totalSpaces = map.get("totalSpaces");
                    Object currentParked = map.get("currentParked");
                    
                    if (totalSpaces instanceof Number && currentParked instanceof Number) {
                        int total = ((Number)totalSpaces).intValue();
                        int parked = ((Number)currentParked).intValue();
                        int available = total - parked;
                        // 음수는 0으로 처리
                        return Math.max(0, available);
                    }
                }
            } catch (Exception e) {
                log.warn("Redis Map 처리 중 오류: {}", e.getMessage());
            }
        }
        
        // RealtimeRes 클래스 처리 (패키지 이름으로 확인)
        String className = redisValue.getClass().getName();
        if (className.contains("RealtimeRes") || className.contains("realtimeinfo")) {
            try {
                // 리플렉션을 사용하여 RealtimeRes 객체에서 여석 정보 추출
                Method getTotalSpacesMethod = null;
                Method getCurrentParkedMethod = null;
                
                // 메서드 이름 후보들 시도
                String[] totalSpacesMethodNames = {"getTotalSpaces", "getTotalSpace", "totalSpaces", "getAvailableSpace"};
                String[] currentParkedMethodNames = {"getCurrentParked", "getCurrentParking", "currentParked", "getOccupiedSpace"};
                
                for (String methodName : totalSpacesMethodNames) {
                    try {
                        getTotalSpacesMethod = redisValue.getClass().getMethod(methodName);
                        if (getTotalSpacesMethod != null) break;
                    } catch (Exception e) {
                        // 다음 후보 시도
                    }
                }
                
                for (String methodName : currentParkedMethodNames) {
                    try {
                        getCurrentParkedMethod = redisValue.getClass().getMethod(methodName);
                        if (getCurrentParkedMethod != null) break;
                    } catch (Exception e) {
                        // 다음 후보 시도
                    }
                }
                
                if (getTotalSpacesMethod != null && getCurrentParkedMethod != null) {
                    Object totalSpacesObj = getTotalSpacesMethod.invoke(redisValue);
                    Object currentParkedObj = getCurrentParkedMethod.invoke(redisValue);
                    
                    Integer totalSpaces = null;
                    Integer currentParked = null;
                    
                    if (totalSpacesObj instanceof Number) {
                        totalSpaces = ((Number)totalSpacesObj).intValue();
                    } else if (totalSpacesObj instanceof String && ((String)totalSpacesObj).matches("\\d+")) {
                        totalSpaces = Integer.parseInt((String)totalSpacesObj);
                    }
                    
                    if (currentParkedObj instanceof Number) {
                        currentParked = ((Number)currentParkedObj).intValue();
                    } else if (currentParkedObj instanceof String && ((String)currentParkedObj).matches("\\d+")) {
                        currentParked = Integer.parseInt((String)currentParkedObj);
                    }
                    
                    if (totalSpaces != null && currentParked != null) {
                        int available = totalSpaces - currentParked;
                        return Math.max(0, available); // 음수는 0으로 처리
                    }
                }
            } catch (Exception e) {
                log.warn("RealtimeRes 객체 처리 중 오류: {}", e.getMessage());
            }
        }
        
        return null;
    }
    
    /**
     * 여러 전략을 사용하여 실시간 여석 정보 조회
     * @param staticId 정적 주차장 ID
     * @param address 주차장 주소
     * @param phone 주차장 전화번호 
     * @return 여석 수 또는 null
     */
    private Integer findAvailableSpots(String staticId, String address, String phone) {
        try {
            // mappingService가 주입되었는지 확인
            if (mappingService == null) {
                log.warn("MappingService is not yet initialized, skipping ID mapping lookup for ID: {}", staticId);
                return null;
            }
            
            // 주소 기반 조회 - 주소가 제공된 경우에만 수행
            if (address != null && !address.isEmpty()) {
                try {
                    String normalizedAddress = normalizeAddress(address);
                    Integer spots = getAvailableSpotsFromRedis(ADDRESS_CACHE_KEY + normalizedAddress);
                    if (spots != null) {
                        // 성공적인 매핑 정보 저장 (Redis 키에서 ID 추출)
                        String extractedRealtimeId = extractIdFromRedisKey(ADDRESS_CACHE_KEY + normalizedAddress);
                        if (extractedRealtimeId != null && mappingService != null) {
                            mappingService.updateMappingFromSuccessfulMatch(staticId, extractedRealtimeId);
                        }
                        return spots;
                    }
                } catch (Exception e) {
                    log.warn("주소 기반 여석 정보 조회 중 오류: {}", e.getMessage());
                }
            }
            
            // 전화번호 기반 조회 - 전화번호가 있는 경우에만 수행
            if (phone != null && !phone.isEmpty()) {
                try {
                    String normalizedPhone = normalizePhoneNumber(phone);
                    Integer spots = getAvailableSpotsFromRedis(PHONE_CACHE_KEY + normalizedPhone);
                    if (spots != null) {
                        // 성공적인 매핑 정보 저장
                        String extractedRealtimeId = extractIdFromRedisKey(PHONE_CACHE_KEY + normalizedPhone);
                        if (extractedRealtimeId != null && mappingService != null) {
                            mappingService.updateMappingFromSuccessfulMatch(staticId, extractedRealtimeId);
                        }
                        return spots;
                    }
                } catch (Exception e) {
                    log.warn("전화번호 기반 여석 정보 조회 중 오류: {}", e.getMessage());
                }
            }
            
            return null;
            
        } catch (Exception e) {
            log.error("여석 정보 조회 중 예상치 못한 오류: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Redis에서 실시간 주차 여석 정보 파싱
     */
    private Integer getAvailableSpotsFromRedis(String redisKey) {
        try {
            Object redisValue = redisTemplate.opsForValue().get(redisKey);
            
            if (redisValue == null) {
                return null;
            }
            
            // 통합 파서 메서드 사용
            Integer spots = parseRedisValue(redisValue);
            if (spots != null) {
                return spots;
            }
            
            return null;
        } catch (Exception e) {
            log.error("Redis 여석 정보 조회 중 오류: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Redis 키에서 실시간 ID 추출 (필요시 구현)
     */
    private String extractIdFromRedisKey(String redisKey) {
        // Redis 키 값이 ID 정보를 포함하고 있다면 추출
        // 실제 구현은 Redis 데이터 구조에 따라 달라짐
        return null;
    }

    // 모든 주차장 정보를 거리 정보와 함께 조회
    public List<ParkingLotRes> getAllParkingLotsWithDistance(double latitude, double longitude) {
        log.info("[성능측정] 모든 주차장 거리 정보 조회 시작: 위도={}, 경도={}", latitude, longitude);
        long startTime = System.currentTimeMillis();
        
        // 새로운 쿼리를 사용하여 데이터베이스에서 거리 계산을 수행
        List<Object[]> parkingLotData = findAllParkingLotsWithDistance(latitude, longitude);
        log.info("[성능측정] 모든 주차장 조회 완료(거리 계산 포함): {} 건, 소요시간: {}ms", 
                parkingLotData.size(), System.currentTimeMillis() - startTime);
        
        // ID 목록 추출
        List<String> staticIds = parkingLotData.stream()
                .map(data -> (String) data[0])
                .collect(Collectors.toList());
                
        // 매핑 정보 일괄 조회
        startTime = System.currentTimeMillis();
        final Map<String, String> mappingsById = new HashMap<>();
        if (mappingService != null) {
            mappingsById.putAll(mappingService.getRealtimeIdsByStaticIds(staticIds));
            log.info("[성능측정] 매핑 정보 일괄 조회 결과: {} 건, 소요시간: {}ms", 
                    mappingsById.size(), System.currentTimeMillis() - startTime);
        } else {
            log.warn("MappingService is not yet initialized, skipping ID mapping lookup");
        }
        
        // Redis에서 여석 정보 일괄 조회를 위한 키 목록 준비
        startTime = System.currentTimeMillis();
        List<String> redisKeys = new ArrayList<>();
        
        // 매핑된 주차장 정보를 캐시에 저장
        Map<String, ParkingLot> parkingLotDetails = parkingLotRepository.findAllById(staticIds)
                .stream()
                .collect(Collectors.toMap(ParkingLot::getId, lot -> lot, (old, newVal) -> newVal));
        
        // 1. 매핑 ID 기반 키 추가
        for (String realtimeId : mappingsById.values()) {
            if (realtimeId != null && !realtimeId.isEmpty()) {
                redisKeys.add(REALTIME_CACHE_KEY + realtimeId);
            }
        }
        
        // 2. 주소/전화번호 기반 키 추가 - 일괄 처리 최적화
        Map<String, String> normalizedAddresses = new HashMap<>();
        Map<String, String> normalizedPhones = new HashMap<>();
        
        for (ParkingLot lot : parkingLotDetails.values()) {
            // 주소 정규화 및 캐싱
            if (lot.getLotAddress() != null && !lot.getLotAddress().isEmpty()) {
                String normalized = normalizeAddress(lot.getLotAddress());
                if (!normalized.isEmpty()) {
                    normalizedAddresses.put(lot.getId(), normalized);
                    redisKeys.add(ADDRESS_CACHE_KEY + normalized);
                }
            }
            
            // 전화번호 정규화 및 캐싱
            if (lot.getPhone() != null && !lot.getPhone().isEmpty()) {
                String normalized = normalizePhoneNumber(lot.getPhone());
                if (!normalized.isEmpty()) {
                    normalizedPhones.put(lot.getId(), normalized);
                    redisKeys.add(PHONE_CACHE_KEY + normalized);
                }
            }
        }
        
        // Redis에서 여석 정보 일괄 조회
        Map<String, Integer> availableSpotsByKey = new HashMap<>();
        if (!redisKeys.isEmpty()) {
            List<Object> redisValues = redisTemplate.opsForValue().multiGet(redisKeys);
            if (redisValues != null) {
                for (int i = 0; i < redisKeys.size(); i++) {
                    if (redisValues.get(i) != null) {
                        try {
                            // 값 파싱 시도
                            Object value = redisValues.get(i);
                            Integer spots = parseRedisValue(value);
                            if (spots != null) {
                                availableSpotsByKey.put(redisKeys.get(i), spots);
                            }
                        } catch (Exception e) {
                            log.warn("Redis 값 파싱 중 오류: {}", e.getMessage());
                        }
                    }
                }
            }
        }
        log.info("[성능측정] Redis 여석 정보 일괄 조회: {}개 키 중 {}개 조회됨, 소요시간: {}ms", 
                redisKeys.size(), availableSpotsByKey.size(), System.currentTimeMillis() - startTime);
        
        // 결과 생성
        startTime = System.currentTimeMillis();
        
        // 주차장 데이터 처리 병렬화하여 속도 개선 (getNearbyParkingLots와 유사한 처리 방식 적용)
        List<ParkingLotRes> result = parkingLotData.parallelStream().map(data -> {
            try {
                if (data == null || data.length < 5) {
                    log.warn("유효하지 않은 주차장 데이터: {}", data);
                    return null;
                }
                
                String staticId = (String) data[0];
                String name = (String) data[1];
                
                // 숫자 타입 안전하게 변환
                Double lat = null;
                if (data[2] != null) {
                    if (data[2] instanceof Number) {
                        lat = ((Number)data[2]).doubleValue();
                    } else {
                        lat = Double.parseDouble(data[2].toString());
                    }
                }
                
                Double lng = null;
                if (data[3] != null) {
                    if (data[3] instanceof Number) {
                        lng = ((Number)data[3]).doubleValue();
                    } else {
                        lng = Double.parseDouble(data[3].toString());
                    }
                }
                
                String address = (String) data[4];
                
                // 필수 데이터 검증
                if (staticId == null || name == null || lat == null || lng == null) {
                    log.warn("주차장 필수 데이터 누락: ID={}, 이름={}, 위도={}, 경도={}", 
                            staticId, name, lat, lng);
                    return null;
                }
                
                // 거리 정보 계산
                Double distance = null;
                if (data.length > 5 && data[5] != null) {
                    if (data[5] instanceof Number) {
                        distance = ((Number)data[5]).doubleValue();
                    } else {
                        try {
                            distance = Double.parseDouble(data[5].toString());
                        } catch (NumberFormatException e) {
                            log.warn("거리 정보 파싱 오류: {}", e.getMessage());
                        }
                    }
                }
                
                // 캐싱된 상세 정보에서 가져오기
                ParkingLot fullLotInfo = parkingLotDetails.get(staticId);
                if (fullLotInfo == null) {
                    log.warn("주차장 상세 정보 누락: ID={}", staticId);
                    return null;
                }
                
                int slotCount = 0;
                if (fullLotInfo.getSlotCount() != null) {
                    slotCount = fullLotInfo.getSlotCount();
                }
                
                // 여석 정보 조회 - 미리 조회한 매핑 정보 활용
                Integer availableSpots = null;
                
                // 1. 매핑된 실시간 ID로 조회
                String realtimeId = mappingsById.get(staticId);
                if (realtimeId != null) {
                    String redisKey = REALTIME_CACHE_KEY + realtimeId;
                    availableSpots = availableSpotsByKey.get(redisKey);
                }
                
                // 2. 주소 기반 조회
                if (availableSpots == null && normalizedAddresses.containsKey(staticId)) {
                    String normalized = normalizedAddresses.get(staticId);
                    String redisKey = ADDRESS_CACHE_KEY + normalized;
                    availableSpots = availableSpotsByKey.get(redisKey);
                }
                
                // 3. 전화번호 기반 조회
                if (availableSpots == null && normalizedPhones.containsKey(staticId)) {
                    String normalized = normalizedPhones.get(staticId);
                    String redisKey = PHONE_CACHE_KEY + normalized;
                    availableSpots = availableSpotsByKey.get(redisKey);
                }
                
                // 평균 리뷰 점수 조회
                Double averageRating = reviewService.getAverageRatingByParkingLotId(staticId);
                
                // 최종 결과 생성 - Builder 패턴 사용
                ParkingLotRes.ParkingLotResBuilder builder = ParkingLotRes.builder()
                    .id(staticId)
                    .name(name)
                    .latitude(lat)
                    .longitude(lng)
                    .address(address)
                    .type(fullLotInfo.getType())
                    .slotCount(slotCount)
                    .availableSpots(availableSpots)
                    .distance(distance)
                    .phone(fullLotInfo.getPhone())
                    .category(fullLotInfo.getCategory())
                    .roadAddress(fullLotInfo.getRoadAddress())
                    .zoneType(fullLotInfo.getZoneType())
                    .rotationType(fullLotInfo.getRotationType())
                    .openDays(fullLotInfo.getOpenDays())
                    .feeInfo(fullLotInfo.getFeeInfo())
                    .baseFee(fullLotInfo.getBaseFee())
                    .baseTime(fullLotInfo.getBaseTime())
                    .addFee(fullLotInfo.getAddFee())
                    .addTime(fullLotInfo.getAddTime())
                    .dayTicketFee(fullLotInfo.getDayTicketFee())
                    .monthTicketFee(fullLotInfo.getMonthTicketFee())
                    .paymentMethod(fullLotInfo.getPaymentMethod())
                    .remarks(fullLotInfo.getRemarks())
                    .adminName(fullLotInfo.getAdminName())
                    .averageRating(averageRating);
                
                return builder.build();
            } catch (Exception e) {
                log.error("주차장 데이터 변환 중 오류 발생: {}", e.getMessage(), e);
                return null;
            }
        })
        .filter(res -> res != null)
        .collect(Collectors.toList());
        
        log.info("[성능측정] 결과 변환 완료: {} 건, 소요시간: {}ms", 
                result.size(), System.currentTimeMillis() - startTime);
        
        return result;
    }
    
    // 데이터베이스 수준에서 모든 주차장 정보와 거리를 조회하는 메소드
    private List<Object[]> findAllParkingLotsWithDistance(double latitude, double longitude) {
        // 데이터베이스에서 직접 쿼리 실행 - 이미 존재하는 Repository를 활용하여 쿼리 실행
        return parkingLotRepository.findAllParkingLotsOrderedByDistance(latitude, longitude);
    }
    
    /**
     * 특정 주차장의 상세 정보를 조회하는 메소드
     * 
     * @param parkingLotId 조회할 주차장 ID
     * @param latitude 사용자 위치 위도
     * @param longitude 사용자 위치 경도
     * @return 주차장 상세 정보
     */
    public ParkingLotRes getParkingLotDetail(String parkingLotId, double latitude, double longitude) {
        log.info("[성능측정] 특정 주차장 상세 정보 조회 시작: ID={}, 위도={}, 경도={}", parkingLotId, latitude, longitude);
        long startTime = System.currentTimeMillis();
        
        // 주차장 기본 정보 조회
        ParkingLot parkingLot = parkingLotRepository.findById(parkingLotId)
                .orElse(null);
                
        if (parkingLot == null) {
            log.warn("주차장 정보를 찾을 수 없습니다: ID={}", parkingLotId);
            return null;
        }
        
        log.info("[성능측정] 특정 주차장 기본 정보 조회 완료: 소요시간={}ms", System.currentTimeMillis() - startTime);
        
        // 매핑 정보 조회
        startTime = System.currentTimeMillis();
        String realtimeId = null;
        if (mappingService != null) {
            Map<String, String> mapping = mappingService.getRealtimeIdsByStaticIds(List.of(parkingLotId));
            realtimeId = mapping.get(parkingLotId);
        }
        
        // 여석 정보 조회를 위한 키 준비
        List<String> redisKeys = new ArrayList<>();
        
        // 1. 매핑된 실시간 ID 기반 키 추가
        if (realtimeId != null && !realtimeId.isEmpty()) {
            redisKeys.add(REALTIME_CACHE_KEY + realtimeId);
        }
        
        // 2. 주소 기반 키
        String normalizedAddress = null;
        if (parkingLot.getLotAddress() != null && !parkingLot.getLotAddress().isEmpty()) {
            normalizedAddress = normalizeAddress(parkingLot.getLotAddress());
            if (!normalizedAddress.isEmpty()) {
                redisKeys.add(ADDRESS_CACHE_KEY + normalizedAddress);
            }
        }
        
        // 3. 전화번호 기반 키
        String normalizedPhone = null;
        if (parkingLot.getPhone() != null && !parkingLot.getPhone().isEmpty()) {
            normalizedPhone = normalizePhoneNumber(parkingLot.getPhone());
            if (!normalizedPhone.isEmpty()) {
                redisKeys.add(PHONE_CACHE_KEY + normalizedPhone);
            }
        }
        
        // Redis에서 여석 정보 조회
        Integer availableSpots = null;
        if (!redisKeys.isEmpty()) {
            List<Object> redisValues = redisTemplate.opsForValue().multiGet(redisKeys);
            if (redisValues != null) {
                for (int i = 0; i < redisKeys.size(); i++) {
                    if (redisValues.get(i) != null) {
                        try {
                            Integer spots = parseRedisValue(redisValues.get(i));
                            if (spots != null) {
                                availableSpots = spots;
                                break; // 첫 번째 유효한 여석 정보 사용
                            }
                        } catch (Exception e) {
                            log.warn("Redis 값 파싱 중 오류: {}", e.getMessage());
                        }
                    }
                }
            }
        }
        
        // 거리 계산
        double distance = 0.0;
        if (parkingLot.getLat() != null && parkingLot.getLng() != null) {
            distance = GeoUtils.calculateDistance(
                    latitude, longitude,
                    parkingLot.getLat(), parkingLot.getLng());
        }
        
        // 평균 리뷰 점수 조회
        Double averageRating = reviewService.getAverageRatingByParkingLotId(parkingLotId);
        
        // 결과 객체 생성
        ParkingLotRes result = ParkingLotRes.builder()
                .id(parkingLot.getId())
                .name(parkingLot.getName())
                .latitude(parkingLot.getLat())
                .longitude(parkingLot.getLng())
                .address(parkingLot.getLotAddress())
                .type(parkingLot.getType())
                .slotCount(parkingLot.getSlotCount() != null ? parkingLot.getSlotCount() : 0)
                .availableSpots(availableSpots)
                .distance(distance)
                .phone(parkingLot.getPhone())
                .category(parkingLot.getCategory())
                .roadAddress(parkingLot.getRoadAddress())
                .zoneType(parkingLot.getZoneType())
                .rotationType(parkingLot.getRotationType())
                .openDays(parkingLot.getOpenDays())
                .feeInfo(parkingLot.getFeeInfo())
                .baseFee(parkingLot.getBaseFee())
                .baseTime(parkingLot.getBaseTime())
                .addFee(parkingLot.getAddFee())
                .addTime(parkingLot.getAddTime())
                .dayTicketFee(parkingLot.getDayTicketFee())
                .monthTicketFee(parkingLot.getMonthTicketFee())
                .paymentMethod(parkingLot.getPaymentMethod())
                .remarks(parkingLot.getRemarks())
                .adminName(parkingLot.getAdminName())
                .averageRating(averageRating)
                .build();
        
        log.info("[성능측정] 특정 주차장 상세 정보 조회 완료: 소요시간={}ms", System.currentTimeMillis() - startTime);
        
        return result;
    }
}