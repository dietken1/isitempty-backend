package com.isitempty.backend.parkinglot.controller;

import com.isitempty.backend.parkinglot.entity.ParkingLot;
import com.isitempty.backend.parkinglot.repository.ParkingLotRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/parking-lots")
public class ParkingLotController {

    private final ParkingLotRepository parkingLotRepository;

    public ParkingLotController(ParkingLotRepository parkingLotRepository) {
        this.parkingLotRepository = parkingLotRepository;
    }

    // 모든 주차장 조회
    @GetMapping
    public List<ParkingLot> getAllParkingLots() {
        return parkingLotRepository.findAll();
    }
    
    // ID로 주차장 조회
    @GetMapping("/{id}")
    public ResponseEntity<ParkingLot> getParkingLotById(@PathVariable Long id) {
        return parkingLotRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // 주변 주차장 검색
    @GetMapping("/nearby")
    public List<ParkingLot> getNearbyParkingLots(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "5.0") Double radius) {
        return parkingLotRepository.findNearbyParkingLots(latitude, longitude, radius);
    }
    
    // 주차장 추가
    @PostMapping
    public ParkingLot createParkingLot(@RequestBody ParkingLot parkingLot) {
        parkingLot.setLastUpdated(LocalDateTime.now());
        return parkingLotRepository.save(parkingLot);
    }
    
    // 주차장 정보 업데이트
    @PutMapping("/{id}")
    public ResponseEntity<ParkingLot> updateParkingLot(
            @PathVariable Long id,
            @RequestBody ParkingLot parkingLotDetails) {
        
        return parkingLotRepository.findById(id)
                .map(parkingLot -> {
                    parkingLot.setName(parkingLotDetails.getName());
                    parkingLot.setAddress(parkingLotDetails.getAddress());
                    parkingLot.setDescription(parkingLotDetails.getDescription());
                    parkingLot.setTotalSpaces(parkingLotDetails.getTotalSpaces());
                    parkingLot.setAvailableSpaces(parkingLotDetails.getAvailableSpaces());
                    parkingLot.setLatitude(parkingLotDetails.getLatitude());
                    parkingLot.setLongitude(parkingLotDetails.getLongitude());
                    parkingLot.setHourlyRate(parkingLotDetails.getHourlyRate());
                    parkingLot.setIsOpen(parkingLotDetails.getIsOpen());
                    return ResponseEntity.ok(parkingLotRepository.save(parkingLot));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    // 사용 가능한 주차 공간 업데이트
    @PatchMapping("/{id}/spaces")
    public ResponseEntity<ParkingLot> updateAvailableSpaces(
            @PathVariable Long id,
            @RequestParam Integer availableSpaces) {
        
        return parkingLotRepository.findById(id)
                .map(parkingLot -> {
                    parkingLot.setAvailableSpaces(availableSpaces);
                    return ResponseEntity.ok(parkingLotRepository.save(parkingLot));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    // 주차장 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteParkingLot(@PathVariable Long id) {
        return parkingLotRepository.findById(id)
                .map(parkingLot -> {
                    parkingLotRepository.delete(parkingLot);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
} 