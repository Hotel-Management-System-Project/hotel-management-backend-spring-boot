package com.hotel.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomImageDTO {
    private Integer imageId;
    private Integer roomId;
    private String imageUrl;
}
