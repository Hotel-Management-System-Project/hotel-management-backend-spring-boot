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
        return completeCheckedOutBookings(repo.findByUser_UserId(userId));
    }

    public List<Booking> getAllBookings() {
        return completeCheckedOutBookings(repo.findAll());
    }

    public List<Booking> getHotelBookings(Integer hotelId) {
        return completeCheckedOutBookings(repo.findByHotelId(hotelId));
    }

    private List<Booking> completeCheckedOutBookings(List<Booking> bookings) {
        LocalDate today = LocalDate.now();
        List<Booking> completed = bookings.stream()
                .filter(booking -> booking.getStatus() == Booking.Status.BOOKED)
                .filter(booking -> booking.getCheckOutDate() != null)
                .filter(booking -> !booking.getCheckOutDate().isAfter(today))
                .toList();

        completed.forEach(booking -> booking.setStatus(Booking.Status.COMPLETED));
        if (!completed.isEmpty()) {
            repo.saveAll(completed);
        }

        return bookings;
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
        existing.setStatus(updated.getStatus());

        return repo.save(existing);
    }

    public void cancelBooking(int id) {
        Booking booking = getById(id);
        booking.setStatus(Booking.Status.CANCELLED);
        repo.save(booking);
    }

    public void completeBooking(int id) {
        Booking booking = getById(id);

        if (booking.getStatus() != Booking.Status.BOOKED) {
            throw new RuntimeException("Only booked reservations can be completed");
        }

        if (booking.getCheckOutDate() == null
                || booking.getCheckOutDate().isAfter(LocalDate.now())) {
            throw new RuntimeException("A booking can be completed on or after its checkout date");
        }

        booking.setStatus(Booking.Status.COMPLETED);
        repo.save(booking);
    }

    public List<Booking> searchByDate(LocalDate from, LocalDate to) {
        return repo.findByCheckInDateBetween(from, to);
    }

    public void deleteBooking(int id) {
        repo.deleteById(id);
    }
}
