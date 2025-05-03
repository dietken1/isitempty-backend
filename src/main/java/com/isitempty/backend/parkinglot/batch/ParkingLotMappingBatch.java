package com.isitempty.backend.parkinglot.batch;

import com.isitempty.backend.parkinglot.entity.ParkingLot;
import com.isitempty.backend.parkinglot.repository.ParkingLotRepository;
import com.isitempty.backend.parkinglot.service.ParkingLotMappingService;
import com.isitempty.backend.parkinglot.service.ParkingLotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class ParkingLotMappingBatch {

    private final ParkingLotRepository parkingLotRepository;
    private ParkingLotService parkingLotService;
    private final ParkingLotMappingService mappingService;
    private final RedisTemplate<String, Object> redisTemplate;
    
    // 키 패턴 정의 - RealtimeInfoScheduler와 동일하게 설정
    private static final String PARKING_ID_PREFIX = "parking:";
    private static final String ADDRESS_PREFIX = "parking:address:";
    private static final String PHONE_PREFIX = "parking:phone:";
    
    @Autowired
    public ParkingLotMappingBatch(ParkingLotRepository parkingLotRepository, 
                                 ParkingLotMappingService mappingService,
                                 RedisTemplate<String, Object> redisTemplate) {
        this.parkingLotRepository = parkingLotRepository;
        this.mappingService = mappingService;
        this.redisTemplate = redisTemplate;
    }
    
    @Autowired
    public void setParkingLotService(ParkingLotService parkingLotService) {
        this.parkingLotService = parkingLotService;
    }
    
    /**
     * 매일 자정에 주차장 ID 매핑 작업 실행
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void runDailyMapping() {
        log.info("시작: 주차장 ID 매핑 배치 작업");
        int mappedCount = 0;
        
        try {
            Set<String> processedIds = new HashSet<>();
            List<ParkingLot> allParkingLots = parkingLotRepository.findAll();
            
            log.info("총 {} 개의 주차장 데이터를 처리합니다", allParkingLots.size());
            
            // 주소 인덱스 존재 여부 확인
            Set<String> addressKeys = redisTemplate.keys(ADDRESS_PREFIX + "*");
            if (addressKeys == null || addressKeys.isEmpty()) {
                log.warn("주소 인덱스 키가 존재하지 않습니다. 주차장 데이터가 인덱싱되지 않았을 수 있습니다.");
            } else {
                log.info("총 {} 개의 주소 인덱스가 존재합니다", addressKeys.size());
            }
            
            // 전화번호 인덱스 존재 여부 확인
            Set<String> phoneKeys = redisTemplate.keys(PHONE_PREFIX + "*");
            if (phoneKeys == null || phoneKeys.isEmpty()) {
                log.warn("전화번호 인덱스 키가 존재하지 않습니다. 주차장 데이터가 인덱싱되지 않았을 수 있습니다.");
            } else {
                log.info("총 {} 개의 전화번호 인덱스가 존재합니다", phoneKeys.size());
            }
            
            for (ParkingLot lot : allParkingLots) {
                // 이미 처리된 ID는 건너뜀
                if (processedIds.contains(lot.getId())) {
                    continue;
                }
                
                boolean mapped = processLot(lot);
                if (mapped) {
                    mappedCount++;
                    processedIds.add(lot.getId());
                }
            }
            
            log.info("완료: {} 개의 주차장 ID 매핑 생성/업데이트", mappedCount);
        } catch (Exception e) {
            log.error("주차장 ID 매핑 배치 작업 중 오류 발생", e);
        }
    }
    
    /**
     * 개별 주차장 매핑 처리
     */
    private boolean processLot(ParkingLot lot) {
        String staticId = lot.getId();
        String lotAddress = lot.getLotAddress();
        String phone = lot.getPhone();
        
        // 이미 매핑 정보가 있는지 확인
        String existingMapping = mappingService.getRealtimeIdByStaticId(staticId);
        if (existingMapping != null) {
            // 이미 신뢰도 높은 매핑이 있으면 처리 안함
            return false;
        }
        
        // 1. 주소 기반 매핑 시도
        if (lotAddress != null && !lotAddress.isEmpty()) {
            String normalizedAddress = parkingLotService.normalizeAddress(lotAddress);
            String redisKey = ADDRESS_PREFIX + normalizedAddress;
            Object realtimeData = redisTemplate.opsForValue().get(redisKey);
            
            if (realtimeData != null) {
                String realtimeId = extractIdFromRedisData(realtimeData, redisKey);
                if (realtimeId != null) {
                    mappingService.saveMapping(staticId, realtimeId, 0.8f);
                    log.info("주소 기반 매핑 성공: {} -> {}", staticId, realtimeId);
                    return true;
                }
            }
        }
        
        // 2. 전화번호 기반 매핑 시도
        if (phone != null && !phone.isEmpty()) {
            String normalizedPhone = parkingLotService.normalizePhoneNumber(phone);
            String redisKey = PHONE_PREFIX + normalizedPhone;
            Object realtimeData = redisTemplate.opsForValue().get(redisKey);
            
            if (realtimeData != null) {
                String realtimeId = extractIdFromRedisData(realtimeData, redisKey);
                if (realtimeId != null) {
                    mappingService.saveMapping(staticId, realtimeId, 0.7f);
                    log.info("전화번호 기반 매핑 성공: {} -> {}", staticId, realtimeId);
                    return true;
                }
            }
        }
        
        // 3. 위치(위도/경도) + 이름 기반 매핑 시도
        // Redis에서 모든 실시간 주차장 데이터를 가져와 비교할 수 있음
        // 실제 구현은 Redis 데이터 구조에 따라 달라짐
        
        return false;
    }
    
    /**
     * Redis 데이터에서 실시간 ID 추출
     */
    private String extractIdFromRedisData(Object redisData, String redisKey) {
        log.debug("Redis 데이터 추출 시도: {}, 데이터 타입: {}", redisKey, 
                  redisData != null ? redisData.getClass().getName() : "null");
                  
        // 실제 Redis 데이터 구조에 맞게 ID 추출 로직 구현
        try {
            // 데이터가 Map이고 id 필드가 있는 경우
            if (redisData instanceof Map<?, ?> map && map.containsKey("id")) {
                return map.get("id").toString();
            }
            
            // 특정 클래스 타입이고 getId 메서드가 있는 경우
            if (redisData != null && redisData.getClass().getMethod("getId") != null) {
                return redisData.getClass().getMethod("getId").invoke(redisData).toString();
            }
            
            // 키 패턴에서 ID 추출 시도 (실제 구현은 Redis 구조에 따라 달라짐)
            if (redisKey.startsWith(PARKING_ID_PREFIX)) {
                String[] parts = redisKey.split(":");
                if (parts.length > 1) {
                    return parts[1]; // parking:ID 형태에서 ID 부분 추출
                }
            }
        } catch (Exception e) {
            log.warn("Redis 데이터에서 ID 추출 실패: {}", e.getMessage());
        }
        
        return null;
    }
} 