package com.hotel.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.hotel.model.Booking;
import com.hotel.repository.BookingRepository;

@Service
public class BookingService {

    private final BookingRepository repo;

    public BookingService(BookingRepository repo) {
        this.repo = repo;
    }

    public Booking createBooking(Booking booking) {
        return repo.save(booking);
    }

    public List<Booking> getUserBookings(Integer userId) {
        return repo.findByUser_UserId(userId); // ✅ FIX
    }

    public List<Booking> getAllBookings() {
        return repo.findAll();
    }

    public Booking getById(int id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
    }

    public Booking updateBooking(int id, Booking updated) {
        Booking existing = getById(id);

        existing.setCheckInDate(updated.getCheckInDate());
        existing.setCheckOutDate(updated.getCheckOutDate());
        existing.setTotalAmount(updated.getTotalAmount());

        return repo.save(existing);
    }

    public void cancelBooking(int id) {
        Booking booking = getById(id);
        booking.setStatus(Booking.Status.CANCELLED);
        repo.save(booking);
    }

    public List<Booking> searchByDate(LocalDate from, LocalDate to) {
        return repo.findByCheckInDateBetween(from, to);
    }

    public void deleteBooking(int id) {
        repo.deleteById(id);
    }
}