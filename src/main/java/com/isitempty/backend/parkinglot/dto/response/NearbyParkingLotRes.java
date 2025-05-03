package com.isitempty.backend.parkinglot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NearbyParkingLotRes {
    private String id;
    private String name;
    private double latitude;
    private double longitude;
    private String address;
    private double distance;       // km 단위 거리
    private int availableSpaces;   // 실시간 여석 정보
}