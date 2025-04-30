package com.isitempty.backend.realtimeinfo.service;

// 테스트 주석
import com.isitempty.backend.realtimeinfo.dto.response.RealtimeRes;
import com.isitempty.backend.realtimeinfo.dto.response.ParkingInfoRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.data.redis.enabled", havingValue = "true", matchIfMissing = true)
// @ConditionalOnBean(RedisTemplate.class) 주석처리
public class RealtimeInfoScheduler {

    @Value("${seoul.key:${SEOUL_KEY:}}")
    private String SEOUL_KEY;

    private final RestTemplate restTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    
    @PostConstruct
    public void init() {
        log.info("RealtimeInfoScheduler가 초기화되었습니다. Redis 활성화됨.");
        log.info("SEOUL_KEY 설정 상태: {}", SEOUL_KEY != null ? "설정됨" : "설정되지 않음");
    }

    // 1분마다 실행
    @Scheduled(fixedRate = 60_000)
    public void fetchAndStoreParkingData() {
        log.info("주차장 정보 업데이트 스케줄러 실행 시작");
        
        // JSON 형식으로 응답을 요청
        String apiUrl = "http://openapi.seoul.go.kr:8088/" + SEOUL_KEY + "/json/GetParkingInfo/1/176";
        log.debug("API 호출: {}", apiUrl);
        
        try {
            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));
            
            // HTTP 요청 엔티티 생성
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            // exchange 메소드로 요청 보내기 (GET 요청)
            ResponseEntity<ParkingInfoRes> responseEntity = 
                restTemplate.exchange(apiUrl, HttpMethod.GET, entity, ParkingInfoRes.class);
            
            ParkingInfoRes response = responseEntity.getBody();
            
            if (response != null && response.getGetParkingInfo() != null) {
                RealtimeRes[] data = response.getGetParkingInfo().getRow();
                log.info("API에서 {} 개의 주차장 정보를 가져왔습니다", data.length);
                
                // 현재 API에서 가져온 주차장 ID를 추적
                Set<String> currentParkingIds = new HashSet<>();
                int updatedCount = 0;
                int unchangedCount = 0;
                
                for (RealtimeRes item : data) {
                    String key = "parking:" + item.getId();
                    currentParkingIds.add(key);
                    
                    // 기존 데이터 조회
                    Object existingData = redisTemplate.opsForValue().get(key);
                    
                    // 데이터가 변경되었거나 존재하지 않는 경우에만 업데이트
                    if (existingData == null || !Objects.equals(existingData, item)) {
                        // TTL 3시간으로 설정 (데이터 업데이트가 실패해도 일정 시간 데이터 유지)
                        redisTemplate.opsForValue().set(key, item, Duration.ofHours(3));
                        updatedCount++;
                        log.debug("Redis 저장 대상 데이터 업데이트: {}", item);
                    } else {
                        // 데이터는 같지만 TTL만 갱신
                        redisTemplate.expire(key, Duration.ofHours(3));
                        unchangedCount++;
                    }
                }
                
                // 더 이상 API에서 제공되지 않는 주차장 데이터 삭제 (주 1회 실행)
                cleanupOldData(currentParkingIds);

                log.info("주차장 정보 업데이트 완료 - 변경: {}건, 유지: {}건", updatedCount, unchangedCount);
            } else {
                log.error("응답 구조에 문제가 있어 GetParkingInfo가 null입니다.");
            }

        } catch (Exception e) {
            log.error("주차장 정보 업데이트 실패: {}", e.getMessage(), e);
        }
    }
    
    // 1주일에 한 번 실행 (더 이상 API에서 제공되지 않는 데이터 정리)
    private boolean cleanupScheduled = false;
    
    private void cleanupOldData(Set<String> currentKeys) {
        // 주 1회만 실행하기 위한 로직
        long currentTimeMillis = System.currentTimeMillis();
        if (!cleanupScheduled && currentTimeMillis % (7 * 24 * 60 * 60 * 1000) < 60_000) {
            cleanupScheduled = true;
            log.info("오래된 Redis 데이터 정리 실행");
            
            try {
                // Redis에서 "parking:"으로 시작하는 모든 키 가져오기
                Set<String> allKeys = redisTemplate.keys("parking:*");
                
                if (allKeys != null) {
                    int removedCount = 0;
                    // 현재 API에 없는 키 삭제
                    for (String key : allKeys) {
                        if (!currentKeys.contains(key)) {
                            redisTemplate.delete(key);
                            removedCount++;
                            log.info("삭제된 오래된 주차장 데이터: {}", key);
                        }
                    }
                    
                    log.info("Redis 데이터 정리 완료 - 삭제: {}건", removedCount);
                }
            } catch (Exception e) {
                log.error("Redis 데이터 정리 실패: {}", e.getMessage(), e);
            }
        } else if (currentTimeMillis % (7 * 24 * 60 * 60 * 1000) >= 60_000) {
            cleanupScheduled = false;
        }
    }
}