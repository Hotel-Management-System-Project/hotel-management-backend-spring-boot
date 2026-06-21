package com.hotel.controller;

import java.time.LocalDate;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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
    private final UserService userService;

    public BookingController(BookingService service,
                             ModelMapper mapper,
                             UserService userService) {
        this.service = service;
        this.mapper = mapper;
        this.userService = userService;
    }

    // 🔐 Get Logged-in User
    private User getUser() {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private boolean isAdmin() {
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    // ✅ CREATE BOOKING
    @PostMapping
    public Resp<?> create(@RequestBody BookingDTO dto) {

        User user = getUser();

        Booking booking = mapper.map(dto, Booking.class);
        booking.setUser(user); // ✅ FIXED

        return Resp.success(
                mapper.map(service.createBooking(booking), BookingDTO.class)
        );
    }

    // ✅ GET MY BOOKINGS
    @GetMapping("/my")
    public Resp<?> getMyBookings() {

        User user = getUser();

        List<BookingDTO> list = service.getUserBookings(user.getUserId())
                .stream()
                .map(b -> mapper.map(b, BookingDTO.class))
                .toList();

        return Resp.success(list);
    }

    // ✅ ADMIN ONLY
    @GetMapping
    public Resp<?> getAll() {

        if (!isAdmin()) {
            return Resp.error("Access Denied");
        }

        List<BookingDTO> list = service.getAllBookings()
                .stream()
                .map(b -> mapper.map(b, BookingDTO.class))
                .toList();

        return Resp.success(list);
    }

    // ✅ GET BY ID
    @GetMapping("/{id}")
    public Resp<?> getById(@PathVariable int id) {

        Booking booking = service.getById(id);

        if (!isAdmin() && 
            !booking.getUser().getUserId().equals(getUser().getUserId())) {
            return Resp.error("Unauthorized");
        }

        return Resp.success(mapper.map(booking, BookingDTO.class));
    }

    // ✅ UPDATE
    @PutMapping("/{id}")
    public Resp<?> update(@PathVariable int id,
                          @RequestBody BookingDTO dto) {

        Booking existing = service.getById(id);

        if (!isAdmin() && 
            !existing.getUser().getUserId().equals(getUser().getUserId())) {
            return Resp.error("Unauthorized");
        }

        Booking updated = mapper.map(dto, Booking.class);

        return Resp.success(
                mapper.map(service.updateBooking(id, updated), BookingDTO.class)
        );
    }

    // ✅ CANCEL
    @PutMapping("/cancel/{id}")
    public Resp<?> cancel(@PathVariable int id) {

        Booking booking = service.getById(id);

        if (!isAdmin() && 
            !booking.getUser().getUserId().equals(getUser().getUserId())) {
            return Resp.error("Unauthorized");
        }

        service.cancelBooking(id);
        return Resp.success("Booking Cancelled");
    }

    // ✅ SEARCH (ADMIN)
    @GetMapping("/search")
    public Resp<?> search(@RequestParam String from,
                          @RequestParam String to) {

        if (!isAdmin()) {
            return Resp.error("Access Denied");
        }

        List<BookingDTO> list = service.searchByDate(
                LocalDate.parse(from),
                LocalDate.parse(to))
                .stream()
                .map(b -> mapper.map(b, BookingDTO.class))
                .toList();

        return Resp.success(list);
    }

    // ✅ DELETE
    @DeleteMapping("/{id}")
    public Resp<?> delete(@PathVariable int id) {

        if (!isAdmin()) {
            return Resp.error("Only Admin");
        }

        service.deleteBooking(id);
        return Resp.success("Deleted Successfully");
    }
    
    
}