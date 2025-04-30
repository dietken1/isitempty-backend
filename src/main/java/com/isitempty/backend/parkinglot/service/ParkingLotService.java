package com.isitempty.backend.parkinglot.service;

import com.isitempty.backend.parkinglot.dto.response.ParkingLotRes;
import com.isitempty.backend.parkinglot.entity.ParkingLot;
import com.isitempty.backend.parkinglot.repository.ParkingLotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParkingLotService {

    private final ParkingLotRepository parkingLotRepository;
    private final RedisTemplate<String, Object> redisTemplate;

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
        if (address == null) return "";
        address = address.toLowerCase().trim()
                .replaceAll("^(서울특별시|경기도|부산광역시|대구광역시|광주광역시|대전광역시|울산광역시|세종특별자치시|제주특별자치도)", "")
                .replaceAll("-0\\b", "")
                .replaceAll("\\s\\(.*?\\)", "");
        return address.trim();
    }

    // 전화번호 정규화
    public String normalizePhoneNumber(String phone) {
        if (phone == null) return "";
        return phone.replaceAll("[^0-9]", "");
    }

    // 실시간 여석 통합 주차장 리스트
    public List<ParkingLotRes> getNearbyParkingLots(float latitude, float longitude) {
        float radius = 2.0f;
        List<ParkingLot> parkingLots = parkingLotRepository.findNearbyParkingLots(latitude, longitude, radius);

        return parkingLots.stream()
                .map(lot -> {
                    String normalizedAddress = normalizeAddress(lot.getLotAddress());
                    String normalizedPhone = normalizePhoneNumber(lot.getPhone());

                    // Redis 키
                    String redisKey1 = "parking_availability:" + normalizedAddress;
                    String redisKey2 = "parking_info:" + normalizedPhone;

                    // Redis 조회 (주소 → 전화번호 순으로 fallback)
                    Object redisValue = redisTemplate.opsForValue().get(redisKey1);
                    if (redisValue == null) {
                        redisValue = redisTemplate.opsForValue().get(redisKey2);
                    }

                    // 값 파싱
                    Integer availableSpots = null;
                    if (redisValue instanceof Integer i) {
                        availableSpots = i;
                    } else if (redisValue instanceof String s && s.matches("\\d+")) {
                        availableSpots = Integer.parseInt(s);
                    }

                    return ParkingLotRes.of(
                            lot.getId(),
                            lot.getName(),
                            lot.getLotAddress(),
                            lot.getLat(),
                            lot.getLng(),
                            lot.getSlotCount(),
                            availableSpots
                    );
                })
                .toList();
    }
}