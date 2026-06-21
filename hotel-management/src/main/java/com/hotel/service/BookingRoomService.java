package com.hotel.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.hotel.model.Booking;
import com.hotel.model.BookingRoom;
import com.hotel.model.Room;
import com.hotel.repository.BookingRepository;
import com.hotel.repository.BookingRoomRepository;

@Service
public class BookingRoomService {

    private final BookingRoomRepository repo;
    private final BookingRepository bookingRepo;

    public BookingRoomService(BookingRoomRepository repo,
                              BookingRepository bookingRepo) {
        this.repo = repo;
        this.bookingRepo = bookingRepo;
    }

    // ✅ CHECK AVAILABILITY
    public boolean isRoomAvailable(Room room, Booking booking) {

        LocalDate checkIn = booking.getCheckInDate();
        LocalDate checkOut = booking.getCheckOutDate();

        List<BookingRoom> conflicts =
                repo.findConflictingBookings(room, checkIn, checkOut);

        return conflicts.isEmpty();
    }

    // ✅ ADD ROOM
    public BookingRoom addRoomToBooking(BookingRoom br) {

        if (!isRoomAvailable(br.getRoom(), br.getBooking())) {
            throw new RuntimeException("Room already booked");
        }

        return repo.save(br);
    }

    // ✅ GET ROOMS BY BOOKING
    public List<BookingRoom> getRoomsByBooking(int bookingId) {

        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        return repo.findByBooking(booking);
    }

    public List<BookingRoom> getAll() {
        return repo.findAll();
    }

    public BookingRoom update(int id, BookingRoom updated) {

        BookingRoom existing = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));

        existing.setPricePerNight(updated.getPricePerNight());

        return repo.save(existing);
    }

    public void removeRoom(int id) {
        repo.deleteById(id);
    }

    public BookingRoom getById(int id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Not Found"));
    }
}