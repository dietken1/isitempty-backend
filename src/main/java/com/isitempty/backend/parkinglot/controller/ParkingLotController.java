package com.isitempty.backend.parkinglot.controller;

import com.isitempty.backend.parkinglot.dto.request.NearbyParkingLotReq;
import com.isitempty.backend.parkinglot.dto.response.ParkingLotRes;
import com.isitempty.backend.parkinglot.entity.ParkingLot;
import com.isitempty.backend.parkinglot.service.ParkingLotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/parking-lots")
public class ParkingLotController {

    private final ParkingLotService parkingLotService;

    // 모든 주차장 조회
    @GetMapping
    public ResponseEntity<List<ParkingLot>> getAll() {
        return ResponseEntity.ok(parkingLotService.getAllParkingLots());
    }

    // ID로 주차장 조회
    @GetMapping("/{id}")
    public ParkingLot getParkingLotById(@PathVariable String id) {
        return parkingLotService.getParkingLotById(id);
    }
    
    // 반경 2km 주변 주차장 검색
    @GetMapping("/nearby")
    public List<ParkingLotRes> getNearbyParkingLots(NearbyParkingLotReq req) {
        return parkingLotService.getNearbyParkingLots(req.getLatitude(), req.getLongitude());
    }
} 