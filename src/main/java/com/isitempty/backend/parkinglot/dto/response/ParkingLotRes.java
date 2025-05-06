package com.isitempty.backend.parkinglot.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ParkingLotRes {
    private String id;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private int slotCount;
    private Integer availableSpots; // 실시간 자리수 (없으면 null)
    
    // 추가된 상세 정보 필드
    private String phone;           // 전화번호
    private String type;            // 주차장 유형
    private String category;        // 주차장 카테고리
    private String roadAddress;     // 도로명 주소
    private String zoneType;        // 구역 유형
    private String rotationType;    // 회전 유형
    private String openDays;        // 영업일
    private String feeInfo;         // 요금 정보
    private Integer baseFee;        // 기본 요금
    private Integer baseTime;       // 기본 시간
    private Integer addFee;         // 추가 요금
    private Integer addTime;        // 추가 시간
    private Integer dayTicketFee;   // 일일권 요금
    private Integer monthTicketFee; // 월권 요금
    private String paymentMethod;   // 결제 방법
    private String remarks;         // 비고
    private String adminName;       // 관리자 이름
    private Double distance;        // 거리(km)
}