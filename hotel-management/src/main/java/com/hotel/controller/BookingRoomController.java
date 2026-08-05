package com.hotel.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
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

    private final BookingRoomService bookingRoomService;
    private final BookingService bookingService;
    private final RoomService roomService;
    private final UserService userService;

    public BookingRoomController(
            BookingRoomService bookingRoomService,
            BookingService bookingService,
            RoomService roomService,
            UserService userService
    ) {
        this.bookingRoomService = bookingRoomService;
        this.bookingService = bookingService;
        this.roomService = roomService;
        this.userService = userService;
    }

    /*
     * Returns the currently authenticated user.
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getName())) {
            throw new RuntimeException("Unauthorized");
        }

        return userService.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }

    /*
     * Checks whether the current authenticated user is an administrator.
     */
    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null) {
            return false;
        }

        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority()
                                .equals("ROLE_ADMIN"));
    }

    /*
     * Converts the BookingRoom entity into a safe response DTO.
     */
    private BookingRoomDTO convertToDTO(BookingRoom bookingRoom) {
        BookingRoomDTO dto = new BookingRoomDTO();

        dto.setBookingRoomId(
                bookingRoom.getBookingRoomId()
        );

        dto.setBookingId(
                bookingRoom.getBooking().getBookingId()
        );

        dto.setRoomId(
                bookingRoom.getRoom().getRoomId()
        );

        dto.setPricePerNight(
                bookingRoom.getPricePerNight()
        );

        return dto;
    }

    /*
     * Connects a room to a booking.
     *
     * BookingRoomService checks availability again while holding
     * a database lock. This prevents simultaneous double bookings.
     */
    @PostMapping
    public Resp<?> addRoomToBooking(
            @RequestBody BookingRoomDTO dto
    ) {
        User currentUser = getCurrentUser();

        Booking booking = bookingService.getById(
                dto.getBookingId()
        );

        boolean ownsBooking =
                booking.getUser() != null &&
                booking.getUser().getUserId()
                        .equals(currentUser.getUserId());

        if (!isAdmin() && !ownsBooking) {
            return Resp.error("Unauthorized");
        }

        Room room = roomService.getRoomEntityById(
                dto.getRoomId()
        );

        BookingRoom bookingRoom = new BookingRoom();
        bookingRoom.setBooking(booking);
        bookingRoom.setRoom(room);
        bookingRoom.setPricePerNight(
                dto.getPricePerNight()
        );

        BookingRoom saved =
                bookingRoomService.addRoomToBooking(
                        bookingRoom
                );

        return Resp.success(convertToDTO(saved));
    }

    /*
     * Checks room availability using an existing booking.
     *
     * Example:
     * GET /api/booking-rooms/check?roomId=10&bookingId=5
     */
    @GetMapping("/check")
    public Resp<?> checkUsingBooking(
            @RequestParam int roomId,
            @RequestParam int bookingId
    ) {
        User currentUser = getCurrentUser();

        Booking booking = bookingService.getById(bookingId);

        boolean ownsBooking =
                booking.getUser() != null &&
                booking.getUser().getUserId()
                        .equals(currentUser.getUserId());

        if (!isAdmin() && !ownsBooking) {
            return Resp.error("Unauthorized");
        }

        Room room = roomService.getRoomEntityById(roomId);

        boolean available =
                bookingRoomService.isRoomAvailable(
                        room,
                        booking
                );

        return Resp.success(available);
    }

    /*
     * Checks availability before creating a booking.
     *
     * Example:
     * GET /api/booking-rooms/availability
     *     ?roomId=10
     *     &checkIn=2026-08-05
     *     &checkOut=2026-08-07
     */
    @GetMapping("/availability")
    public Resp<?> checkAvailability(
            @RequestParam int roomId,

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate checkIn,

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate checkOut
    ) {
        // Confirms that this request comes from a logged-in user.
        getCurrentUser();

        Room room = roomService.getRoomEntityById(roomId);

        boolean available =
                bookingRoomService.isRoomAvailable(
                        room,
                        checkIn,
                        checkOut
                );

        return Resp.success(available);
    }

    /*
     * Returns rooms connected to a particular booking.
     */
    @GetMapping("/{bookingId}")
    public Resp<?> getRoomsByBooking(
            @PathVariable int bookingId
    ) {
        User currentUser = getCurrentUser();

        Booking booking = bookingService.getById(bookingId);

        boolean ownsBooking =
                booking.getUser() != null &&
                booking.getUser().getUserId()
                        .equals(currentUser.getUserId());

        if (!isAdmin() && !ownsBooking) {
            return Resp.error("Unauthorized");
        }

        List<BookingRoomDTO> rooms =
                bookingRoomService
                        .getRoomsByBooking(bookingId)
                        .stream()
                        .map(this::convertToDTO)
                        .toList();

        return Resp.success(rooms);
    }

    /*
     * Returns every booking-room record.
     * Only an administrator can use this endpoint.
     */
    @GetMapping
    public Resp<?> getAll() {
        getCurrentUser();

        if (!isAdmin()) {
            return Resp.error(
                    "Only administrators can access this resource"
            );
        }

        List<BookingRoomDTO> bookingRooms =
                bookingRoomService.getAll()
                        .stream()
                        .map(this::convertToDTO)
                        .toList();

        return Resp.success(bookingRooms);
    }

    /*
     * Updates the saved room price in a booking.
     */
    @PutMapping("/{bookingRoomId}")
    public Resp<?> update(
            @PathVariable int bookingRoomId,
            @RequestBody BookingRoomDTO dto
    ) {
        User currentUser = getCurrentUser();

        Booking booking = bookingService.getById(
                dto.getBookingId()
        );

        boolean ownsBooking =
                booking.getUser() != null &&
                booking.getUser().getUserId()
                        .equals(currentUser.getUserId());

        if (!isAdmin() && !ownsBooking) {
            return Resp.error("Unauthorized");
        }

        BookingRoom updated = new BookingRoom();
        updated.setPricePerNight(
                dto.getPricePerNight()
        );

        BookingRoom saved =
                bookingRoomService.update(
                        bookingRoomId,
                        updated
                );

        return Resp.success(convertToDTO(saved));
    }

    /*
     * Removes a room from a booking.
     */
    @DeleteMapping("/{bookingRoomId}")
    public Resp<?> delete(
            @PathVariable int bookingRoomId
    ) {
        BookingRoom bookingRoom =
                bookingRoomService.getById(
                        bookingRoomId
                );

        User currentUser = getCurrentUser();

        boolean ownsBooking =
                bookingRoom.getBooking().getUser() != null &&
                bookingRoom.getBooking()
                        .getUser()
                        .getUserId()
                        .equals(currentUser.getUserId());

        if (!isAdmin() && !ownsBooking) {
            return Resp.error("Unauthorized");
        }

        bookingRoomService.removeRoom(bookingRoomId);

        return Resp.success(
                "Room removed from booking successfully"
        );
    }
}