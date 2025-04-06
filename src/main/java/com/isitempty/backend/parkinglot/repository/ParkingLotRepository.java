package com.isitempty.backend.parkinglot.repository;

import com.isitempty.backend.parkinglot.entity.ParkingLot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParkingLotRepository extends JpaRepository<ParkingLot, Long> {
    
    // 주변 주차장 검색 (위도, 경도, 반경(km) 기준)
    @Query(value = "SELECT * FROM parking_lots p WHERE " +
            "(6371 * acos(cos(radians(?1)) * cos(radians(p.latitude)) * cos(radians(p.longitude) - radians(?2)) + sin(radians(?1)) * sin(radians(p.latitude)))) < ?3", 
            nativeQuery = true)
    List<ParkingLot> findNearbyParkingLots(Double latitude, Double longitude, Double radius);
    
    // 이름으로 주차장 검색
    List<ParkingLot> findByNameContaining(String name);
    
    // 주소로 주차장 검색
    List<ParkingLot> findByAddressContaining(String address);
    
    // 사용 가능한 주차 공간이 있는 주차장 검색
    List<ParkingLot> findByAvailableSpacesGreaterThan(Integer spaces);
} 