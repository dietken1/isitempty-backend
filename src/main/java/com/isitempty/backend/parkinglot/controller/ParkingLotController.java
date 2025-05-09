package com.isitempty.backend.parkinglot.controller;

import com.isitempty.backend.parkinglot.dto.request.NearbyParkingLotReq;
import com.isitempty.backend.parkinglot.dto.response.ParkingLotRes;
import com.isitempty.backend.parkinglot.entity.ParkingLot;
import com.isitempty.backend.parkinglot.service.ParkingLotService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/parking-lots")
public class ParkingLotController {

    private final ParkingLotService parkingLotService;
    private static final Logger log = LoggerFactory.getLogger(ParkingLotController.class);

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
    
    // 반경 2km 주변 주차장 검색 (쿼리 파라미터 방식)
    @GetMapping("/nearby")
    public ResponseEntity<List<ParkingLotRes>> getNearbyParkingLotsWithQueryParams(
            @RequestParam(required = false, defaultValue = "37.5665") double latitude,
            @RequestParam(required = false, defaultValue = "126.9780") double longitude) {
        try {
            log.info("GET 요청 - 주변 주차장 검색 요청: 위도={}, 경도={}", latitude, longitude);
            List<ParkingLotRes> result = parkingLotService.getNearbyParkingLots(latitude, longitude);
            log.info("주변 주차장 검색 결과: {}건", result.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("주변 주차장 검색 API 오류: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // 반경 2km 주변 주차장 검색 (JSON 요청 본문 방식)
    @PostMapping("/nearby")
    public ResponseEntity<List<ParkingLotRes>> getNearbyParkingLots(@RequestBody NearbyParkingLotReq req) {
        try {
            log.info("POST 요청 - 주변 주차장 검색 요청: 위도={}, 경도={}", req.getLatitude(), req.getLongitude());
            List<ParkingLotRes> result = parkingLotService.getNearbyParkingLots(req.getLatitude(), req.getLongitude());
            log.info("주변 주차장 검색 결과: {}건", result.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("주변 주차장 검색 API 오류: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // 모든 주차장을 거리 정보와 함께 제공 (쿼리 파라미터 방식)
    @GetMapping("/all-with-distance")
    public ResponseEntity<List<ParkingLotRes>> getAllParkingLotsWithDistance(
            @RequestParam(required = false, defaultValue = "37.5665") double latitude,
            @RequestParam(required = false, defaultValue = "126.9780") double longitude) {
        try {
            log.info("GET 요청 - 모든 주차장 거리 정보 요청: 위도={}, 경도={}", latitude, longitude);
            List<ParkingLotRes> result = parkingLotService.getAllParkingLotsWithDistance(latitude, longitude);
            log.info("모든 주차장 거리 정보 결과: {}건", result.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("모든 주차장 거리 정보 API 오류: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // 모든 주차장을 거리 정보와 함께 제공 (JSON 요청 본문 방식)
    @PostMapping("/all-with-distance")
    public ResponseEntity<List<ParkingLotRes>> getAllParkingLotsWithDistance(
            @RequestBody NearbyParkingLotReq req) {
        try {
            log.info("POST 요청 - 모든 주차장 거리 정보 요청: 위도={}, 경도={}", req.getLatitude(), req.getLongitude());
            List<ParkingLotRes> result = parkingLotService.getAllParkingLotsWithDistance(req.getLatitude(), req.getLongitude());
            log.info("모든 주차장 거리 정보 결과: {}건", result.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("모든 주차장 거리 정보 API 오류: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // 특정 주차장의 상세 정보 조회 (쿼리 파라미터 방식)
    @GetMapping("/{id}/detail")
    public ResponseEntity<ParkingLotRes> getParkingLotDetail(
            @PathVariable String id,
            @RequestParam(required = false, defaultValue = "37.5665") double latitude,
            @RequestParam(required = false, defaultValue = "126.9780") double longitude) {
        try {
            log.info("GET 요청 - 특정 주차장 상세 정보 요청: ID={}, 위도={}, 경도={}", id, latitude, longitude);
            ParkingLotRes result = parkingLotService.getParkingLotDetail(id, latitude, longitude);
            if (result == null) {
                log.warn("주차장을 찾을 수 없음: ID={}", id);
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("특정 주차장 상세 정보 조회 API 오류: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // 특정 주차장의 상세 정보 조회 (JSON 요청 본문 방식)
    @PostMapping("/{id}/detail")
    public ResponseEntity<ParkingLotRes> getParkingLotDetailWithBody(
            @PathVariable String id, 
            @RequestBody NearbyParkingLotReq req) {
        try {
            log.info("POST 요청 - 특정 주차장 상세 정보 요청: ID={}, 위도={}, 경도={}", 
                    id, req.getLatitude(), req.getLongitude());
            ParkingLotRes result = parkingLotService.getParkingLotDetail(id, req.getLatitude(), req.getLongitude());
            if (result == null) {
                log.warn("주차장을 찾을 수 없음: ID={}", id);
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("특정 주차장 상세 정보 조회 API 오류: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
} 