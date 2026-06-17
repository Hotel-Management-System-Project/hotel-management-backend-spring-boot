package com.hotel.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="booking_room")
public class BookingRoom {

	 @Id
	 @GeneratedValue(strategy = GenerationType.IDENTITY)
	 private int bookingRoomId;
	 
	 private int bookingId;   
	 
	 private int roomId;
	 
	 private double pricePerNight;
	
	 
}
