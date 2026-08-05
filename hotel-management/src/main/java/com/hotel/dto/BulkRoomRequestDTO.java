package com.hotel.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Creates several rooms with shared details and sequential room numbers.
 * Example: startRoomNumber 101 and quantity 100 creates rooms 101 through 200.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkRoomRequestDTO {

    @NotNull
    private Integer hotelId;

    @NotNull
    @Min(1)
    private Integer startRoomNumber;

    @NotNull
    @Min(value = 1, message = "Create at least one room")
    @Max(value = 100, message = "A maximum of 100 rooms can be created at once")
    private Integer quantity;

    @NotBlank
    @Size(max = 50)
    private String roomType;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal pricePerNight;

    @NotNull
    @Min(1)
    private Integer capacity;

    @NotNull
    private Boolean availabilityStatus;
    
    @NotNull(message = "Select AC or Non-AC")
    private Boolean airConditioned;

    private Boolean hasWifi = true;

    @NotNull(message = "Select TV or No TV")
    private Boolean hasTv;
}
