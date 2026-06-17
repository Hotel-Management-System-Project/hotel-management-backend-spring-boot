package com.hotel.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "hotels")
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long hotelId;

    private Long ownerId;

    private String hotelName;

    private String description;

    private String address;

    private String city;

    private String state;

    private String pincode;

    private BigDecimal rating;

    @Enumerated(EnumType.STRING)
    private HotelStatus status;

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum HotelStatus {
        pending,
        approved,
        rejected
    }
}