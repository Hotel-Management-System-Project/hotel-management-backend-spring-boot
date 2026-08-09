package com.hotel.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private String reply;
    private List<ChatHotelResult> hotels;
    private List<String> suggestions;
    private boolean aiEnabled;
}
