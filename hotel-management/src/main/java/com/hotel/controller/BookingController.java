package com.hotel.controller;

import java.time.LocalDate;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.hotel.dto.BookingDTO;
import com.hotel.model.Booking;
import com.hotel.service.BookingService;
import com.hotel.utils.Resp;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin("*")
public class BookingController {

    private final BookingService service;
    private final ModelMapper mapper;

    public BookingController(BookingService service, ModelMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    // 🔐 Get Logged-in User ID from JWT
    private Integer getUserId() {
        return (Integer) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

    private boolean isAdmin() {
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    // ✅ CREATE BOOKING (SECURE)
    @PostMapping
    public Resp<BookingDTO> create(@RequestBody BookingDTO dto) {

        Integer userId = getUserId();

        Booking booking = mapper.map(dto, Booking.class);
        booking.setUserId(userId);

        return Resp.success(
                mapper.map(service.createBooking(booking), BookingDTO.class)
        );
    }

    // ✅ GET MY BOOKINGS (SECURE)
    @GetMapping("/my")
    public Resp<List<BookingDTO>> getMyBookings() {

        Integer userId = getUserId();

        List<BookingDTO> list = service.getUserBookings(userId)
                .stream()
                .map(b -> mapper.map(b, BookingDTO.class))
                .toList();

        return Resp.success(list);
    }

    // ✅ ADMIN ONLY - GET ALL BOOKINGS
    @GetMapping
    public Resp<List<BookingDTO>> getAll() {

        if (!isAdmin()) {
            return Resp.error("Access Denied");
        }

        List<BookingDTO> list = service.getAllBookings()
                .stream()
                .map(b -> mapper.map(b, BookingDTO.class))
                .toList();

        return Resp.success(list);
    }

    // ✅ GET BY ID (SECURE OWNER CHECK)
    @GetMapping("/{id}")
    public Resp<?> getById(@PathVariable int id) {

        Booking booking = service.getById(id);

        if (!isAdmin() && booking.getUserId() != getUserId()) {
            return Resp.error("Unauthorized Access");
        }

        return Resp.success(mapper.map(booking, BookingDTO.class));
    }

    // ✅ UPDATE (OWNER OR ADMIN)
    @PutMapping("/{id}")
    public Resp<?> update(@PathVariable int id,
                          @RequestBody BookingDTO dto) {

        Booking existing = service.getById(id);

        if (!isAdmin() && existing.getUserId() != getUserId()) {
            return Resp.error("Unauthorized");
        }

        Booking booking = mapper.map(dto, Booking.class);

        return Resp.success(
                mapper.map(service.updateBooking(id, booking), BookingDTO.class)
        );
    }

    // ✅ CANCEL (OWNER OR ADMIN)
    @PutMapping("/cancel/{id}")
    public Resp<?> cancel(@PathVariable int id) {

        Booking booking = service.getById(id);

        if (!isAdmin() && booking.getUserId() != getUserId()) {
            return Resp.error("Unauthorized");
        }

        service.cancelBooking(id);
        return Resp.success("Booking Cancelled Successfully");
    }

    // ✅ SEARCH (ADMIN ONLY)
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

    // ✅ DELETE (ADMIN ONLY)
    @DeleteMapping("/{id}")
    public Resp<?> delete(@PathVariable int id) {

        if (!isAdmin()) {
            return Resp.error("Only Admin can delete");
        }

        service.deleteBooking(id);
        return Resp.success("Booking Deleted Successfully");
    }
}