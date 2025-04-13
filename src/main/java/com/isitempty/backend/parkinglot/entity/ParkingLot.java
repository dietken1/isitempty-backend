package com.isitempty.backend.parkinglot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "parking_lots")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkingLot {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String address;
    
    private String description;
    
    @Column(nullable = false)
    private Integer totalSpaces;
    
    @Column(nullable = false)
    private Integer availableSpaces;
    
    private Double latitude;
    
    private Double longitude;
    
    private Double hourlyRate;
    
    @Column(nullable = false)
    private Boolean isOpen;
    
    @Column(nullable = false)
    private LocalDateTime lastUpdated;
    
    @PrePersist
    @PreUpdate
    public void updateTimestamp() {
        lastUpdated = LocalDateTime.now();
    }
} 