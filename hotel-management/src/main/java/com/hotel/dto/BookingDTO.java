package com.hotel.dto;

import java.time.LocalDate;

import com.hotel.model.Booking.Status;

import lombok.Data;

@Data
public class BookingDTO {

    private Integer bookingId;
    private Integer userId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private Integer roomId;
    private Integer roomNumber;
    private String roomType;

    private LocalDate checkInDate;
    private LocalDate checkOutDate;

    private double totalAmount;
    
    private Status status;
}
