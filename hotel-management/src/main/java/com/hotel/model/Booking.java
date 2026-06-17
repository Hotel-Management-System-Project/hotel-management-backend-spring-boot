package com.hotel.model;

import java.time.LocalDate;
import java.time.LocalDateTime;


import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "bookings")
public class Booking {
	
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int bookingId;
	
	private int userId;
	
	private LocalDate checkInDate;
	 
	private LocalDate checkOutDate;
	
	@Enumerated(EnumType.STRING)
	private Status status;

    private double totalAmount;
    
    private LocalDateTime bookingDate=LocalDateTime.now();
    
    public enum Status {
        BOOKED, CANCELLED, COMPLETED
    }

}
