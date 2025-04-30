package com.isitempty.backend.realtimeinfo.controller;

import com.isitempty.backend.realtimeinfo.service.RealtimeInfoScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final RealtimeInfoScheduler scheduler;

    @GetMapping("/trigger-scheduler")
    public String triggerScheduler() {
        log.info("수동으로 스케줄러 트리거");
        try {
            scheduler.fetchAndStoreParkingData();
            return "스케줄러 실행 성공";
        } catch (Exception e) {
            log.error("스케줄러 실행 중 오류 발생: {}", e.getMessage(), e);
            return "스케줄러 실행 실패: " + e.getMessage();
        }
    }
}