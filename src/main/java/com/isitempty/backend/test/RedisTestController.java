package com.isitempty.backend.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/test")
public class RedisTestController {

    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public RedisTestController(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/redis")
    public Map<String, Object> testRedis() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String testKey = "test:key";
            String testValue = "Redis is working! " + System.currentTimeMillis();
            
            // Redis에 테스트 값 저장
            redisTemplate.opsForValue().set(testKey, testValue);
            
            // Redis에서 테스트 값 조회
            Object retrievedValue = redisTemplate.opsForValue().get(testKey);
            
            response.put("success", true);
            response.put("message", "Redis 연결 성공");
            response.put("savedValue", testValue);
            response.put("retrievedValue", retrievedValue);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Redis 연결 실패: " + e.getMessage());
            response.put("errorType", e.getClass().getSimpleName());
        }
        
        return response;
    }
    
    @GetMapping("/redis/keys")
    public Map<String, Object> getAllKeys() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Set<String> allKeys = redisTemplate.keys("*");
            
            response.put("success", true);
            response.put("totalKeys", allKeys != null ? allKeys.size() : 0);
            response.put("keys", allKeys);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Redis 키 조회 실패: " + e.getMessage());
            response.put("errorType", e.getClass().getSimpleName());
        }
        
        return response;
    }
    
    @GetMapping("/redis/keys/{pattern}")
    public Map<String, Object> getKeysByPattern(@PathVariable String pattern) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            
            response.put("success", true);
            response.put("pattern", pattern);
            response.put("totalKeys", keys != null ? keys.size() : 0);
            response.put("keys", keys);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Redis 키 패턴 조회 실패: " + e.getMessage());
            response.put("errorType", e.getClass().getSimpleName());
        }
        
        return response;
    }
    
    @GetMapping("/redis/parking")
    public Map<String, Object> getAllParkingData() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Set<String> parkingKeys = redisTemplate.keys("parking:*");
            
            if (parkingKeys != null && !parkingKeys.isEmpty()) {
                List<Object> parkingData = redisTemplate.opsForValue().multiGet(parkingKeys);
                
                Map<String, Object> parkingMap = new HashMap<>();
                int index = 0;
                for (String key : parkingKeys) {
                    if (index < parkingData.size()) {
                        parkingMap.put(key, parkingData.get(index));
                    }
                    index++;
                }
                
                response.put("success", true);
                response.put("totalParking", parkingKeys.size());
                response.put("parkingData", parkingMap);
            } else {
                response.put("success", true);
                response.put("totalParking", 0);
                response.put("message", "주차장 데이터가 없습니다.");
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "주차장 데이터 조회 실패: " + e.getMessage());
            response.put("errorType", e.getClass().getSimpleName());
        }
        
        return response;
    }
    
    @GetMapping("/redis/value/{key}")
    public Map<String, Object> getValueByKey(@PathVariable String key) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Object value = redisTemplate.opsForValue().get(key);
            
            if (value != null) {
                response.put("success", true);
                response.put("key", key);
                response.put("value", value);
                response.put("type", value.getClass().getName());
            } else {
                response.put("success", false);
                response.put("message", "키에 해당하는 값이 없습니다: " + key);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Redis 값 조회 실패: " + e.getMessage());
            response.put("errorType", e.getClass().getSimpleName());
        }
        
        return response;
    }
} 