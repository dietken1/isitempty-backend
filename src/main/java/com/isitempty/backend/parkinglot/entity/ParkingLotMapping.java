package com.isitempty.backend.parkinglot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "parking_lot_mappings")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParkingLotMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "static_id", unique = true)
    private String staticId;

    @Column(name = "realtime_id")
    private String realtimeId;

    @Column(name = "confidence")
    private Float confidence;

    @Column(name = "last_updated")
    private java.time.LocalDateTime lastUpdated;

    public ParkingLotMapping(String staticId, String realtimeId, Float confidence) {
        this.staticId = staticId;
        this.realtimeId = realtimeId;
        this.confidence = confidence;
        this.lastUpdated = java.time.LocalDateTime.now();
    }
} 