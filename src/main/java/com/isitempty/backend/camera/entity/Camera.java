package com.isitempty.backend.camera.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "cameras")
@Getter @Setter
public class Camera {
    @Id
    public Long id;

    public String name;
    public String address;

    @Column(name = "lat")
    public float latitude;

    @Column(name = "lng")
    public float longitude;
}
