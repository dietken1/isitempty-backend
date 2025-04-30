package com.isitempty.backend.parkinglot.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "parking_lots")
@Getter @Setter
public class ParkingLot {

    @Id
    private String id;

    private String name;
    private String type;
    private String category;

    @Column(name = "road_address")
    private String roadAddress;

    @Column(name = "lot_address")
    private String lotAddress;

    @Column(name = "slot_count")
    private Integer slotCount;

    @Column(name = "zone_type")
    private String zoneType;

    @Column(name = "rotation_type")
    private String rotationType;

    @Column(name = "open_days")
    private String openDays;

    @Column(name = "weekday_start")
    private LocalTime weekdayStart;

    @Column(name = "weekday_end")
    private LocalTime weekdayEnd;

    @Column(name = "saturday_start")
    private LocalTime saturdayStart;

    @Column(name = "saturday_end")
    private LocalTime saturdayEnd;

    @Column(name = "holiday_start")
    private LocalTime holidayStart;

    @Column(name = "holiday_end")
    private LocalTime holidayEnd;

    @Column(name = "fee_info")
    private String feeInfo;

    @Column(name = "base_time")
    private Integer baseTime;

    @Column(name = "base_fee")
    private Integer baseFee;

    @Column(name = "add_time")
    private Integer addTime;

    @Column(name = "add_fee")
    private Integer addFee;

    @Column(name = "day_ticket_time")
    private Integer dayTicketTime;

    @Column(name = "day_ticket_fee")
    private Integer dayTicketFee;

    @Column(name = "month_ticket_fee")
    private Integer monthTicketFee;

    @Column(name = "payment_method")
    private String paymentMethod;

    private String remarks;

    @Column(name = "admin_name")
    private String adminName;

    private String phone;
    private Double lat;
    private Double lng;

    @Column(name = "has_disabled_zone")
    private Boolean hasDisabledZone;

    @Column(name = "data_date")
    private LocalDate dataDate;

    @Column(name = "provider_code")
    private String providerCode;

    @Column(name = "provider_name")
    private String providerName;
}