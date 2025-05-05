package com.isitempty.backend.toilet.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "toilets")
@Getter @Setter
public class Toilet {
    @Id
    public Long id;

    public String name;
    public String address;

    @Column(name = "lat")
    public double latitude;

    @Column(name = "lng")
    public double longitude;
}
