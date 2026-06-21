package com.hotel.controller;

import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.hotel.model.Hotel;
import com.hotel.service.HotelService;
import com.hotel.utils.Resp;

@RestController
@RequestMapping("/api/hotels")
public class HotelController {

    private final HotelService service;

    public HotelController(HotelService service) {
        this.service = service;
    }

    private boolean isAdmin() {
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    @PostMapping
    public Resp<?> add(@RequestBody Hotel hotel) {
        return Resp.success(service.addHotel(hotel));
    }

    @GetMapping
    public Resp<List<Hotel>> getAll() {
        return Resp.success(service.getAll());
    }

    @PutMapping("/approve/{id}")
    public Resp<?> approve(@PathVariable Integer id) {
        if (!isAdmin()) return Resp.error("Only Admin");
        return Resp.success(service.approve(id));
    }

    @PutMapping("/reject/{id}")
    public Resp<?> reject(@PathVariable Integer id) {
        if (!isAdmin()) return Resp.error("Only Admin");
        return Resp.success(service.reject(id));
    }
}