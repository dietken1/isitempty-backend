package com.isitempty.backend.realtimeinfo.controller;

import com.isitempty.backend.realtimeinfo.service.RealtimeInfoViewer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/realtimeinfo")
public class RealtimeInfoController {

    private final RealtimeInfoViewer viewer;

    // Redis의 모든 실시간 데이터를 반환해주는 API
    @GetMapping()
    public ResponseEntity<List<Object>> getAllParkingLots() {
        return ResponseEntity.ok(viewer.getAllParkingLots());
    }
}