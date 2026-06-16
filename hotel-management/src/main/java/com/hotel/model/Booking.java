package com.hotel.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import ch.qos.logback.core.status.Status;
import jakarta.persistence.Entity;
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
	
	private int BookingId;
	
	private int UserId;
	
	private LocalDate checkInDate;
	 
	private LocalDate checkOutDate;
	
    private double totalAmount;
    
    private LocalDateTime bookingDate=LocalDateTime.now();
	   
	  
	
}
