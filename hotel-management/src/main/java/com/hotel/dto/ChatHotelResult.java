package com.hotel.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatHotelResult {
    private Integer hotelId;
    private String hotelName;
    private String city;
    private String state;
    private Double rating;
    private String description;
    private BigDecimal lowestPrice;
    private Integer matchingRoomCount;
    private Integer availableRoomCount;
    private Boolean availabilityChecked;
    private String checkIn;
    private String checkOut;
}
