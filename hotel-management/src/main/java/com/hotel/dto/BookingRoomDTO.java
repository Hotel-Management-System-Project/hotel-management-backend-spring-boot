package com.hotel.dto;

import lombok.Data;

@Data
public class BookingRoomDTO {

    private Integer bookingRoomId;
    private Integer bookingId;
    private Integer roomId;

    private double pricePerNight;
}