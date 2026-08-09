package com.hotel.dto;
import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class RoomResponseDTO {

    private Integer roomId;

    private Integer hotelId;

    private Integer roomNumber;

    private String roomType;

    private BigDecimal pricePerNight;

    private Integer capacity;

    private Boolean availabilityStatus;

    // These are returned when an existing room is opened in the web edit
    // form. Without them the form submits null values that fail validation.
    private Boolean airConditioned;

    private Boolean hasWifi;

    private Boolean hasTv;

    private LocalDate createdAt;
}
