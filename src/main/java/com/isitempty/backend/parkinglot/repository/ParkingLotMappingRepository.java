package com.isitempty.backend.parkinglot.repository;

import com.isitempty.backend.parkinglot.entity.ParkingLotMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParkingLotMappingRepository extends JpaRepository<ParkingLotMapping, Long> {
    Optional<ParkingLotMapping> findByStaticId(String staticId);
    Optional<ParkingLotMapping> findByRealtimeId(String realtimeId);
    
    // 여러 정적 ID로 매핑 정보 조회 (일괄 조회)
    List<ParkingLotMapping> findAllByStaticIdIn(List<String> staticIds);
} 