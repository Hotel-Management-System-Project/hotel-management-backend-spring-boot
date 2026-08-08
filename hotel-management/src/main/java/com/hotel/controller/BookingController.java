package com.hotel.controller;

import java.time.LocalDate;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.hotel.dto.BookingDTO;
import com.hotel.model.Booking;
import com.hotel.model.Hotel;
import com.hotel.model.Role;
import com.hotel.model.User;
import com.hotel.service.BookingService;
import com.hotel.service.HotelService;
import com.hotel.service.UserService;
import com.hotel.utils.Resp;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin("*")
public class BookingController {

    private final BookingService service;
    private final ModelMapper mapper;
    private final UserService userService;
    private final HotelService hotelService;

    public BookingController(BookingService service,
                             ModelMapper mapper,
                             UserService userService,
                             HotelService hotelService) {
        this.service = service;
        this.mapper = mapper;
        this.userService = userService;
        this.hotelService = hotelService;
    }

    // 🔐 Get Logged-in User
//    private User getUser() {
//        String email = SecurityContextHolder
//                .getContext()
//                .getAuthentication()
//                .getName();
//
//        return userService.findByEmail(email)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//    }
    
    private User getUser() {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        System.out.println("Email from SecurityContext = " + email);

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

    private boolean isOwner() {
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_HOTEL_OWNER"));
    }

    private boolean ownsBookingHotel(Booking booking, User owner) {
        return booking.getBookingRooms() != null
                && booking.getBookingRooms().stream().anyMatch(bookingRoom ->
                        bookingRoom.getRoom() != null
                                && bookingRoom.getRoom().getHotel() != null
                                && bookingRoom.getRoom().getHotel().getOwner() != null
                                && bookingRoom.getRoom().getHotel().getOwner().getUserId()
                                        .equals(owner.getUserId()));
    }

    private BookingDTO toDTO(Booking booking) {
        BookingDTO dto = mapper.map(booking, BookingDTO.class);
        if (booking.getUser() != null) {
            dto.setUserId(booking.getUser().getUserId());
            dto.setCustomerName(booking.getUser().getFullName());
            dto.setCustomerEmail(booking.getUser().getEmail());
            dto.setCustomerPhone(booking.getUser().getPhone());
        }
        return dto;
    }

    private BookingDTO toHotelDTO(Booking booking, Integer hotelId) {
        BookingDTO dto = toDTO(booking);
        if (booking.getBookingRooms() != null) {
            booking.getBookingRooms().stream()
                    .filter(br -> br.getRoom() != null
                            && br.getRoom().getHotel() != null
                            && br.getRoom().getHotel().getHotelId().equals(hotelId))
                    .findFirst()
                    .ifPresent(br -> {
                        dto.setRoomId(br.getRoom().getRoomId());
                        dto.setRoomNumber(br.getRoom().getRoomNumber());
                        dto.setRoomType(br.getRoom().getRoomType());
                    });
        }
        return dto;
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

    @GetMapping("/hotel/{hotelId}")
    public Resp<?> getByHotel(@PathVariable Integer hotelId) {
        User loggedInUser = getUser();
        boolean admin = loggedInUser.getRole() == Role.ADMIN;
        boolean owner = loggedInUser.getRole() == Role.HOTEL_OWNER;

        if (!admin && !owner) {
            return Resp.error("Access denied");
        }

        Hotel hotel = hotelService.getById(hotelId);
        if (!admin
                && (hotel.getOwner() == null
                || !hotel.getOwner().getUserId().equals(loggedInUser.getUserId()))) {
            return Resp.error("You can view bookings only for your own hotel");
        }

        List<BookingDTO> list = service.getHotelBookings(hotelId)
                .stream()
                .map(booking -> toHotelDTO(booking, hotelId))
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

    @PutMapping("/complete/{id}")
    public Resp<?> complete(@PathVariable int id) {
        Booking booking = service.getById(id);
        User loggedInUser = getUser();

        if (!isAdmin() && (!isOwner() || !ownsBookingHotel(booking, loggedInUser))) {
            return Resp.error("Only the booking hotel owner can complete this booking");
        }

        service.completeBooking(id);
        return Resp.success("Booking completed");
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
