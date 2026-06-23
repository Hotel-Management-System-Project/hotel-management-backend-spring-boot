package com.hotel.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HotelResponse {

    private Integer  hotelId;
    private Integer  ownerId;
    private String hotelName;
    private String description;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private BigDecimal rating;
    private String status;
    private LocalDateTime createdAt;
}