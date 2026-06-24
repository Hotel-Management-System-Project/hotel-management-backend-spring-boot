package com.hotel.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
	 private Integer bookingRoomId;
	 
	 @ManyToOne
	 @JoinColumn(name = "booking_id")
	 @JsonIgnore
	 private Booking booking;

	 @ManyToOne
	 @JoinColumn(name = "room_id")
	 @JsonIgnore
	 private Room room;
	 
	 
	 private double pricePerNight;
	
	 
}
