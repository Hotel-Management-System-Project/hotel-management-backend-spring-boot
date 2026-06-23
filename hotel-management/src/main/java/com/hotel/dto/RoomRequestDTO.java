package com.hotel.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
@Data
public class RoomRequestDTO {

	@NotNull(message = "Hotel Id is required ")
	private Integer hotelId;
	
	
	@NotNull(message = "Room Number is required")
	private Integer roomNumber;
	
	
	@NotBlank(message = "Room type cannot be blank")
	@Size(max = 50, message = "Room type must not exceed 50 characters")
	private String roomType;
	
	
	@NotNull(message = "Price per Night is required ")
	@DecimalMin(value="0.0" ,inclusive = false ,message = "Price per night must be greater than 0")
	private BigDecimal pricePerNight;
	
	@NotNull(message = "Capacity is required ")
	@Min(value=1,message = "Capacity must be at least 1")
	private Integer capacity ;
	
	
	@NotNull(message = "Avalability Status is required ")
	private Boolean  availabilityStatus;
	


	
	
	
}
