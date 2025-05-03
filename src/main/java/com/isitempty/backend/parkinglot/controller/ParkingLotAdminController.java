package com.isitempty.backend.parkinglot.controller;

import com.isitempty.backend.parkinglot.batch.ParkingLotMappingBatch;
import com.isitempty.backend.parkinglot.dto.response.MappingStatusRes;
import com.isitempty.backend.parkinglot.service.ParkingLotMappingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/parking-lots")
public class ParkingLotAdminController {

    private final ParkingLotMappingBatch parkingLotMappingBatch;
    private final ParkingLotMappingService mappingService;

    /**
     * 주차장 ID 매핑 배치 작업을 수동으로 실행하는 API
     * 이 API는 관리자만 접근할 수 있도록 보안 설정이 필요합니다.
     */
    @PostMapping("/run-mapping-batch")
    public ResponseEntity<Map<String, Object>> runMappingBatch() {
        log.info("관리자 요청으로 주차장 ID 매핑 배치 작업 시작");
        
        try {
            // 배치 작업 실행
            parkingLotMappingBatch.runDailyMapping();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "주차장 ID 매핑 배치 작업이 성공적으로 실행되었습니다.");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("매핑 배치 작업 실행 중 오류 발생", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "배치 작업 실행 중 오류가 발생했습니다: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 현재 매핑 상태를 조회하는 API
     */
    @GetMapping("/mapping-status")
    public ResponseEntity<MappingStatusRes> getMappingStatus() {
        log.info("매핑 상태 조회 요청");
        MappingStatusRes status = mappingService.getMappingStatus();
        return ResponseEntity.ok(status);
    }
} 