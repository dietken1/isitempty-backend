package com.isitempty.backend.parkinglot.service;

import com.isitempty.backend.parkinglot.dto.response.MappingStatusRes;
import com.isitempty.backend.parkinglot.entity.ParkingLot;
import com.isitempty.backend.parkinglot.entity.ParkingLotMapping;
import com.isitempty.backend.parkinglot.repository.ParkingLotMappingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ParkingLotMappingService {

    private final ParkingLotMappingRepository mappingRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ParkingLotService parkingLotService;
    
    private static final String MAPPING_CACHE_KEY = "parking:id_mapping:";

    @Autowired
    public ParkingLotMappingService(ParkingLotMappingRepository mappingRepository, 
                                   RedisTemplate<String, Object> redisTemplate,
                                   @Lazy ParkingLotService parkingLotService) {
        this.mappingRepository = mappingRepository;
        this.redisTemplate = redisTemplate;
        this.parkingLotService = parkingLotService;
    }

    /**
     * 정적 ID로 실시간 ID 조회
     */
    public String getRealtimeIdByStaticId(String staticId) {
        // 1. Redis 캐시 확인
        String cacheKey = MAPPING_CACHE_KEY + staticId;
        Object cachedValue = redisTemplate.opsForValue().get(cacheKey);
        
        if (cachedValue != null) {
            return cachedValue.toString();
        }
        
        // 2. DB 조회
        Optional<ParkingLotMapping> mapping = mappingRepository.findByStaticId(staticId);
        if (mapping.isPresent()) {
            // 캐시 갱신
            redisTemplate.opsForValue().set(cacheKey, mapping.get().getRealtimeId());
            return mapping.get().getRealtimeId();
        }
        
        return null;
    }
    
    /**
     * 매핑 정보 저장/업데이트
     */
    @Transactional
    public void saveMapping(String staticId, String realtimeId, float confidence) {
        ParkingLotMapping mapping = mappingRepository.findByStaticId(staticId)
                .orElse(new ParkingLotMapping(staticId, realtimeId, confidence));
        
        mapping.setRealtimeId(realtimeId);
        mapping.setConfidence(confidence);
        mapping.setLastUpdated(LocalDateTime.now());
        
        mappingRepository.save(mapping);
        
        // Redis 캐시 갱신
        redisTemplate.opsForValue().set(MAPPING_CACHE_KEY + staticId, realtimeId);
    }
    
    /**
     * 매핑 정보 통계 업데이트
     * (주소나 전화번호로 성공적으로 매칭된 정보를 저장)
     */
    @Transactional
    public void updateMappingFromSuccessfulMatch(String staticId, String realtimeId) {
        Optional<ParkingLotMapping> existingMapping = mappingRepository.findByStaticId(staticId);
        
        if (existingMapping.isPresent()) {
            ParkingLotMapping mapping = existingMapping.get();
            // 기존 매핑이 있으면 신뢰도 증가
            mapping.setConfidence(Math.min(1.0f, mapping.getConfidence() + 0.1f));
            mapping.setLastUpdated(LocalDateTime.now());
            mappingRepository.save(mapping);
        } else {
            // 새 매핑 추가 (중간 신뢰도로 시작)
            saveMapping(staticId, realtimeId, 0.5f);
        }
    }

    /**
     * 매핑 상태 통계 조회
     */
    public MappingStatusRes getMappingStatus() {
        List<ParkingLotMapping> allMappings = mappingRepository.findAll();
        
        long totalMappings = allMappings.size();
        long highConfidence = allMappings.stream().filter(m -> m.getConfidence() >= 0.8f).count();
        long mediumConfidence = allMappings.stream().filter(m -> m.getConfidence() >= 0.5f && m.getConfidence() < 0.8f).count();
        long lowConfidence = allMappings.stream().filter(m -> m.getConfidence() < 0.5f).count();
        
        return MappingStatusRes.of(totalMappings, highConfidence, mediumConfidence, lowConfidence);
    }
    
    /**
     * 주기적으로 Redis 캐시 갱신
     */
    @Scheduled(fixedRate = 86400000) // 매일 실행
    public void refreshMappingCache() {
        log.info("Refreshing parking lot ID mapping cache...");
        mappingRepository.findAll().forEach(mapping -> {
            redisTemplate.opsForValue().set(
                MAPPING_CACHE_KEY + mapping.getStaticId(), 
                mapping.getRealtimeId()
            );
        });
        log.info("Mapping cache refresh completed");
    }

    /**
     * 여러 정적 ID에 대한 실시간 ID 일괄 조회
     * N+1 문제를 방지하기 위한 배치 처리 메서드
     */
    public Map<String, String> getRealtimeIdsByStaticIds(List<String> staticIds) {
        if (staticIds == null || staticIds.isEmpty()) {
            return new HashMap<>();
        }
        
        Map<String, String> result = new HashMap<>();
        
        // 1. Redis 캐시에서 먼저 조회
        List<String> cacheKeys = staticIds.stream()
            .map(id -> MAPPING_CACHE_KEY + id)
            .collect(Collectors.toList());
        
        if (!cacheKeys.isEmpty()) {
            List<Object> cachedValues = redisTemplate.opsForValue().multiGet(cacheKeys);
            
            if (cachedValues != null) {
                int hitCount = 0;
                for (int i = 0; i < staticIds.size(); i++) {
                    if (cachedValues.get(i) != null) {
                        result.put(staticIds.get(i), cachedValues.get(i).toString());
                        hitCount++;
                    }
                }
                log.info("Redis 캐시 조회 결과: {} 개 히트, {} 개 미스", hitCount, staticIds.size() - hitCount);
            }
        }
        
        // 2. 캐시에 없는 ID들만 DB에서 조회
        List<String> uncachedIds = staticIds.stream()
            .filter(id -> !result.containsKey(id))
            .collect(Collectors.toList());
        
        if (!uncachedIds.isEmpty()) {
            List<ParkingLotMapping> mappings = mappingRepository.findAllByStaticIdIn(uncachedIds);
            log.info("DB 조회 결과: {} 개의 매핑 정보 조회됨", mappings.size());
            
            // DB에서 조회한 결과 캐싱 및 결과 맵에 추가
            for (ParkingLotMapping mapping : mappings) {
                String staticId = mapping.getStaticId();
                String realtimeId = mapping.getRealtimeId();
                
                result.put(staticId, realtimeId);
                redisTemplate.opsForValue().set(MAPPING_CACHE_KEY + staticId, realtimeId);
            }
        }
        
        return result;
    }
} 