package com.hotel.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HotelRequest {

    private Long ownerId;
    private String hotelName;
    private String description;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private BigDecimal rating;
}