package com.hotel.controller;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.hotel.dto.BookingRoomDTO;
import com.hotel.model.Booking;
import com.hotel.model.BookingRoom;
import com.hotel.service.BookingRoomService;
import com.hotel.service.BookingService;
import com.hotel.utils.Resp;

@RestController
@RequestMapping("/api/booking-rooms")
@CrossOrigin("*")
public class BookingRoomController {

    private final BookingRoomService service;
    private final BookingService bookingService;
    private final ModelMapper mapper;

    public BookingRoomController(BookingRoomService service,
                                 BookingService bookingService,
                                 ModelMapper mapper) {
        this.service = service;
        this.bookingService = bookingService;
        this.mapper = mapper;
    }

    // 🔐 Get user from JWT
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

    // ✅ ADD ROOM TO BOOKING (OWNER ONLY)
    @PostMapping
    public Resp<?> add(@RequestBody BookingRoomDTO dto) {

        Booking booking = bookingService.getById(dto.getBookingId());

        if (!isAdmin() && booking.getUserId() != getUserId()) {
            return Resp.error("Unauthorized");
        }

        BookingRoom br = mapper.map(dto, BookingRoom.class);

        return Resp.success(
                mapper.map(service.addRoomToBooking(br), BookingRoomDTO.class)
        );
    }

    // ✅ CHECK ROOM AVAILABILITY (OWNER OR ADMIN)
    @GetMapping("/check")
    public Resp<?> check(@RequestParam int roomId,
                         @RequestParam int bookingId) {

        Booking booking = bookingService.getById(bookingId);

        if (!isAdmin() && booking.getUserId() != getUserId()) {
            return Resp.error("Unauthorized");
        }

        return Resp.success(service.isRoomAvailable(roomId, bookingId));
    }

    // ✅ GET ROOMS BY BOOKING (OWNER ONLY)
    @GetMapping("/{bookingId}")
    public Resp<?> getByBooking(@PathVariable int bookingId) {

        Booking booking = bookingService.getById(bookingId);

        if (!isAdmin() && booking.getUserId() != getUserId()) {
            return Resp.error("Unauthorized");
        }

        List<BookingRoomDTO> list = service.getRoomsByBooking(bookingId)
                .stream()
                .map(b -> mapper.map(b, BookingRoomDTO.class))
                .toList();

        return Resp.success(list);
    }

    // ✅ ADMIN ONLY - GET ALL
    @GetMapping
    public Resp<?> getAll() {

        if (!isAdmin()) {
            return Resp.error("Only Admin can view all");
        }

        List<BookingRoomDTO> list = service.getAll()
                .stream()
                .map(b -> mapper.map(b, BookingRoomDTO.class))
                .toList();

        return Resp.success(list);
    }

    // ✅ UPDATE (OWNER OR ADMIN)
    @PutMapping("/{id}")
    public Resp<?> update(@PathVariable int id,
                          @RequestBody BookingRoomDTO dto) {

        Booking booking = bookingService.getById(dto.getBookingId());

        if (!isAdmin() && booking.getUserId() != getUserId()) {
            return Resp.error("Unauthorized");
        }

        BookingRoom br = mapper.map(dto, BookingRoom.class);

        return Resp.success(
                mapper.map(service.update(id, br), BookingRoomDTO.class)
        );
    }

    // ✅ DELETE (OWNER OR ADMIN)
    @DeleteMapping("/{id}")
    public Resp<?> delete(@PathVariable int id) {

        BookingRoom br = service.getAll().stream()
                .filter(r -> r.getBookingRoomId() == id)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Not Found"));

        Booking booking = bookingService.getById(br.getBookingId());

        if (!isAdmin() && booking.getUserId() != getUserId()) {
            return Resp.error("Unauthorized");
        }

        service.removeRoom(id);
        return Resp.success("Room removed from booking");
    }
}