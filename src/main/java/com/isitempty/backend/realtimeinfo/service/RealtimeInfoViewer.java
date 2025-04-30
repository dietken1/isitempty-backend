package com.isitempty.backend.realtimeinfo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RealtimeInfoViewer {

    private final RedisTemplate<String, Object> redisTemplate;

    public List<Object> getAllParkingLots() {
        // 1. parking:* 키 전체 조회
        Set<String> keys = redisTemplate.keys("parking:*");

        if (keys == null || keys.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 각 키에 해당하는 값 가져오기
        return redisTemplate.opsForValue().multiGet(keys);
    }
}