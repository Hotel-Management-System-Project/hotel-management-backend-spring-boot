package com.hotel.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hotel.model.HotelImage;
import com.hotel.service.HotelImageService;
import com.hotel.utils.Resp;

@RestController
@RequestMapping("/api/hotel-images")
public class HotelImageController {

    private final HotelImageService service;

    public HotelImageController(HotelImageService service) {
        this.service = service;
    }

    @PostMapping
    public Resp<?> add(@RequestBody HotelImage img) {
        return Resp.success(service.add(img));
    }

    @GetMapping("/{hotelId}")
    public Resp<?> get(@PathVariable Integer hotelId) {
        return Resp.success(service.getByHotel(hotelId));
    }
}
