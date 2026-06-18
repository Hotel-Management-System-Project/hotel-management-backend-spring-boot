package com.hotel.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;

import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.*;

import com.hotel.config.JwtUtil;
import com.hotel.dto.BookingDTO;
import com.hotel.model.Booking;
import com.hotel.model.User;
import com.hotel.service.BookingService;
import com.hotel.service.UserService;
import com.hotel.utils.Resp;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin("*")
public class BookingController {

    private final BookingService service;
    private final ModelMapper mapper;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public BookingController(BookingService service,
                             ModelMapper mapper,
                             JwtUtil jwtUtil,
                             UserService userService) {
        this.service = service;
        this.mapper = mapper;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    // 🔑 COMMON METHOD (avoid duplicate code)
    private User getUserFromToken(HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid or missing token");
        }

        String token = authHeader.substring(7);
        String email = jwtUtil.extractEmail(token);

        return userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ✅ CREATE BOOKING
    @PostMapping
    public Resp<BookingDTO> create(HttpServletRequest request,
                                   @RequestBody BookingDTO dto) {

        User user = getUserFromToken(request);

        Booking booking = mapper.map(dto, Booking.class);
        booking.setUserId(user.getUserId());

        Booking saved = service.createBooking(booking);

        return Resp.success(mapper.map(saved, BookingDTO.class));
    }

    // ✅ GET MY BOOKINGS
    @GetMapping("/my")
    public Resp<List<BookingDTO>> getMyBookings(HttpServletRequest request) {

        User user = getUserFromToken(request);

        List<BookingDTO> list = service.getUserBookings(user.getUserId())
                .stream()
                .map(b -> mapper.map(b, BookingDTO.class))
                .collect(Collectors.toList());

        return Resp.success(list);
    }

    @GetMapping
    public Resp<List<BookingDTO>> getAll() {
        return Resp.success(
                service.getAllBookings().stream()
                        .map(b -> mapper.map(b, BookingDTO.class))
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/{id}")
    public Resp<BookingDTO> getById(@PathVariable int id) {
        return Resp.success(mapper.map(service.getById(id), BookingDTO.class));
    }

    @PutMapping("/{id}")
    public Resp<BookingDTO> update(@PathVariable int id,
                                   @RequestBody BookingDTO dto) {

        return Resp.success(
                mapper.map(service.updateBooking(id, mapper.map(dto, Booking.class)), BookingDTO.class)
        );
    }

    @PutMapping("/cancel/{id}")
    public Resp<String> cancel(@PathVariable int id) {
        service.cancelBooking(id);
        return Resp.success("Booking Cancelled Successfully");
    }

    @DeleteMapping("/{id}")
    public Resp<String> delete(@PathVariable int id) {
        service.deleteBooking(id);
        return Resp.success("Booking Deleted Successfully");
    }

    @GetMapping("/search")
    public Resp<List<BookingDTO>> search(@RequestParam String from,
                                         @RequestParam String to) {

        return Resp.success(
                service.searchByDate(LocalDate.parse(from), LocalDate.parse(to))
                        .stream()
                        .map(b -> mapper.map(b, BookingDTO.class))
                        .collect(Collectors.toList())
        );
    }
}