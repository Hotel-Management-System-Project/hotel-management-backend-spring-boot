package com.hotel.controller;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.hotel.dto.BookingRoomDTO;
import com.hotel.model.Booking;
import com.hotel.model.BookingRoom;
import com.hotel.model.Room;
import com.hotel.model.User;
import com.hotel.service.BookingRoomService;
import com.hotel.service.BookingService;
import com.hotel.service.RoomService;
import com.hotel.service.UserService;
import com.hotel.utils.Resp;

@RestController
@RequestMapping("/api/booking-rooms")
@CrossOrigin("*")
public class BookingRoomController {

    private final BookingRoomService service;
    private final BookingService bookingService;
    private final RoomService roomService;
    private final UserService userService;

    public BookingRoomController(BookingRoomService service,
                                 BookingService bookingService,
                                 RoomService roomService,
                                 UserService userService) {
        this.service = service;
        this.bookingService = bookingService;
        this.roomService = roomService;
        this.userService = userService;
    }

    // 🔐 Get User
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

    // ✅ DTO Mapper
    private BookingRoomDTO convertToDTO(BookingRoom br) {
        BookingRoomDTO dto = new BookingRoomDTO();
        dto.setBookingRoomId(br.getBookingRoomId());
        dto.setBookingId(br.getBooking().getBookingId());
        dto.setRoomId(br.getRoom().getRoomId());
        dto.setPricePerNight(br.getPricePerNight());
        return dto;
    }

    // ✅ ADD ROOM
    @PostMapping
    public Resp<?> add(@RequestBody BookingRoomDTO dto) {

        User user = getUser();
        Booking booking = bookingService.getById(dto.getBookingId());

        if (!isAdmin() &&
            !booking.getUser().getUserId().equals(user.getUserId())) {
            return Resp.error("Unauthorized");
        }

        Room room = roomService.getRoomEntityById(dto.getRoomId());

        BookingRoom br = new BookingRoom();
        br.setBooking(booking);
        br.setRoom(room);
        br.setPricePerNight(dto.getPricePerNight());

        return Resp.success(convertToDTO(service.addRoomToBooking(br)));
    }

    // ✅ CHECK AVAILABILITY
 // ONLY CHANGE THIS METHOD

    @GetMapping("/check")
    public Resp<?> check(@RequestParam int roomId,
                         @RequestParam int bookingId) {

        User user = getUser();
        Booking booking = bookingService.getById(bookingId);

        if (!isAdmin() &&
            !booking.getUser().getUserId().equals(user.getUserId())) {
            return Resp.error("Unauthorized");
        }

        // ✅ FIX: fetch entity
        Room room = roomService.getRoomEntityById(roomId);

        return Resp.success(
            service.isRoomAvailable(room, booking)
        );
    }
    

    // ✅ GET ROOMS
    @GetMapping("/{bookingId}")
    public Resp<?> getByBooking(@PathVariable int bookingId) {

        User user = getUser();
        Booking booking = bookingService.getById(bookingId);

        if (!isAdmin() &&
            !booking.getUser().getUserId().equals(user.getUserId())) {
            return Resp.error("Unauthorized");
        }

        List<BookingRoomDTO> list = service.getRoomsByBooking(bookingId)
                .stream()
                .map(this::convertToDTO)
                .toList();

        return Resp.success(list);
    }

    // ✅ ADMIN
    @GetMapping
    public Resp<?> getAll() {

        if (!isAdmin()) {
            return Resp.error("Only Admin");
        }

        return Resp.success(
                service.getAll().stream()
                        .map(this::convertToDTO)
                        .toList()
        );
    }

    // ✅ UPDATE
    @PutMapping("/{id}")
    public Resp<?> update(@PathVariable int id,
                          @RequestBody BookingRoomDTO dto) {

        User user = getUser();
        Booking booking = bookingService.getById(dto.getBookingId());

        if (!isAdmin() &&
            !booking.getUser().getUserId().equals(user.getUserId())) {
            return Resp.error("Unauthorized");
        }

        Room room = roomService.getRoomEntityById(dto.getRoomId());

        BookingRoom br = new BookingRoom();
        br.setBooking(booking);
        br.setRoom(room);
        br.setPricePerNight(dto.getPricePerNight());

        return Resp.success(convertToDTO(service.update(id, br)));
    }

    // ✅ DELETE
    @DeleteMapping("/{id}")
    public Resp<?> delete(@PathVariable int id) {

        BookingRoom br = service.getById(id);
        User user = getUser();

        if (!isAdmin() &&
            !br.getBooking().getUser().getUserId().equals(user.getUserId())) {
            return Resp.error("Unauthorized");
        }

        service.removeRoom(id);
        return Resp.success("Room removed from booking");
    }

}