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

    private LocalDate createdAt;
}