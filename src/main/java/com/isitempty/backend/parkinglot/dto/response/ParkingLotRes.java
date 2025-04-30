package com.isitempty.backend.parkinglot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(staticName = "of")
public class ParkingLotRes {
    private String id;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private int slotCount;
    private Integer availableSpots; // 실시간 자리수 (없으면 null)
}