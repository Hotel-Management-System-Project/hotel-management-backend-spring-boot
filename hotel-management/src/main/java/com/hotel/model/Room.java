package com.hotel.model; 

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
public class Room {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="room_id")
	private Integer roomId;
//	int hotelId;
//	@ManyToOne
//    @JoinColumn(name = "hotel_id")
//    private Hotel hotel;
	
	@Column(name="room_number")
	private Integer roomNumber;
	
	
	@Column(name="room_type")
	private String roomType;
	
	
	
	@Column(name="price_per_night")
	private BigDecimal pricePerNight;
	
	
	@Column(name="capacity")
	private int capacity;
	
	@Column(name="availability_status")
	private boolean availabilityStatus;

	
	

	@Column(name = "created_at")
	private LocalDate createdAt;

	@PrePersist
	public void prePersist() {
	    createdAt = LocalDate.now();
	}
}
