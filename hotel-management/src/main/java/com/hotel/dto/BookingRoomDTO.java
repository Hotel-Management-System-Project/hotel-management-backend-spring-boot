package com.hotel.dto;

import lombok.Data;

@Data
public class BookingRoomDTO {

    private int bookingRoomId;
    private int bookingId;
    private int roomId;

    private double pricePerNight;
}