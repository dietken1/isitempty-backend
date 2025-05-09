package com.isitempty.backend.favorite.entity;

import com.isitempty.backend.oauthlogin.api.entity.user.User;
import com.isitempty.backend.parkinglot.entity.ParkingLot;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Entity
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id; // 찜 고유 ID (String으로 변경)

    @ManyToOne
    private User user; // 유저

    @ManyToOne
    @JoinColumn(name = "parking_lot_id")
    private ParkingLot parkingLot; // 주차장

    private boolean isFavorited = true; // 찜 여부 (true: 찜, false: 찜 해제)
}

