package com.isitempty.backend.review.entity;

import com.isitempty.backend.oauthlogin.api.entity.user.User;
import com.isitempty.backend.parkinglot.entity.ParkingLot;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_lot_id")
    private ParkingLot parkingLot;

    private int rating;
    private String content;
    private LocalDateTime createdAt = LocalDateTime.now();
}