package com.hotel.dto;

import java.time.LocalDate;

import com.hotel.model.Booking.Status;

import lombok.Data;

@Data
public class BookingDTO {

    private int bookingId;
    private int userId;

    private LocalDate checkInDate;
    private LocalDate checkOutDate;

    private double totalAmount;
    
    private Status status;
}