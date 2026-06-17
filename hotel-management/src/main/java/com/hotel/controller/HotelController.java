package com.hotel.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hotel.dto.ApiResponse;
import com.hotel.dto.HotelRequest;
import com.hotel.dto.HotelResponse;
import com.hotel.service.HotelService;

@RestController
@RequestMapping("/api/hotels")
public class HotelController {

    private final HotelService hotelService;

    public HotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<HotelResponse>> createHotel(
            @RequestBody HotelRequest request) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Hotel created successfully",
                        hotelService.createHotel(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<HotelResponse>>> getAllHotels() {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Hotels fetched successfully",
                        hotelService.getAllHotels()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<HotelResponse>> getHotelById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Hotel found",
                        hotelService.getHotelById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<HotelResponse>> updateHotel(
            @PathVariable Long id,
            @RequestBody HotelRequest request) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Hotel updated successfully",
                        hotelService.updateHotel(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteHotel(
            @PathVariable Long id) {

        hotelService.deleteHotel(id);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Hotel deleted successfully",
                        null));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<HotelResponse>> approveHotel(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Hotel approved successfully",
                        hotelService.approveHotel(id)));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<HotelResponse>> rejectHotel(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Hotel rejected successfully",
                        hotelService.rejectHotel(id)));
    }
}