package com.isitempty.backend.parkinglot.repository;

import com.isitempty.backend.parkinglot.entity.ParkingLot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParkingLotRepository extends JpaRepository<ParkingLot, String> {
    
    // 주변 주차장 검색 (위도, 경도, 반경(km) 기준)
    @Query(value = "SELECT p.id, p.name, p.lat, p.lng, p.lot_address, " +
            "(6371 * acos(cos(radians(?1)) * cos(radians(p.lat)) * cos(radians(p.lng) - radians(?2)) + sin(radians(?1)) * sin(radians(p.lat)))) AS distance " +
            "FROM parking_lots p " +
            "WHERE (6371 * acos(cos(radians(?1)) * cos(radians(p.lat)) * cos(radians(p.lng) - radians(?2)) + sin(radians(?1)) * sin(radians(p.lat)))) < ?3 " +
            "ORDER BY distance ASC", nativeQuery = true)
    List<Object[]> findNearbyParkingLots(double latitude, double longitude, double radius);
    
    // 주변 주차장 검색 (위도, 경도 기준 정렬 - 인덱스 활용 최적화)
    @Query(value = "SELECT p.id, p.name, p.lat, p.lng, p.lot_address, " +
            "(6371 * acos(cos(radians(?1)) * cos(radians(p.lat)) * cos(radians(p.lng) - radians(?2)) + sin(radians(?1)) * sin(radians(p.lat)))) AS distance " +
            "FROM parking_lots p " +
            "WHERE p.lat BETWEEN (?1 - 1.0) AND (?1 + 1.0) AND p.lng BETWEEN (?2 - 1.0) AND (?2 + 1.0) " + 
            "ORDER BY distance ASC", nativeQuery = true)
    List<Object[]> findAllParkingLotsOrderedByDistance(double latitude, double longitude);

}