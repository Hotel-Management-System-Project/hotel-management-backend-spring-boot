package com.hotel.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotel.model.Booking;
import com.hotel.model.BookingRoom;
import com.hotel.model.Room;
import com.hotel.repository.BookingRepository;
import com.hotel.repository.BookingRoomRepository;
import com.hotel.repository.RoomRepoRepository;

@Service
public class BookingRoomService {

    private final BookingRoomRepository bookingRoomRepository;
    private final BookingRepository bookingRepository;
    private final RoomRepoRepository roomRepository;

    public BookingRoomService(
            BookingRoomRepository bookingRoomRepository,
            BookingRepository bookingRepository,
            RoomRepoRepository roomRepository
    ) {
        this.bookingRoomRepository = bookingRoomRepository;
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
    }

    /*
     * Checks availability using dates from an existing booking.
     */
    public boolean isRoomAvailable(Room room, Booking booking) {
        return isRoomAvailable(
                room,
                booking.getCheckInDate(),
                booking.getCheckOutDate()
        );
    }

    /*
     * Checks whether a room has an overlapping BOOKED reservation.
     */
    public boolean isRoomAvailable(
            Room room,
            LocalDate checkIn,
            LocalDate checkOut
    ) {
        if (room == null) {
            throw new IllegalArgumentException("Room is required");
        }

        if (checkIn == null || checkOut == null) {
            throw new IllegalArgumentException(
                    "Check-in and check-out dates are required"
            );
        }

        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException(
                    "Check-out must be after check-in"
            );
        }

        // Owner/admin can manually disable a room.
        if (!Boolean.TRUE.equals(room.getAvailabilityStatus())) {
            return false;
        }

        List<BookingRoom> conflicts =
                bookingRoomRepository.findConflictingBookings(
                        room,
                        checkIn,
                        checkOut,
                        Booking.Status.BOOKED
                );

        return conflicts.isEmpty();
    }

    /*
     * Locks the room record and checks availability again before saving.
     * This prevents simultaneous bookings for the same room and dates.
     */
    @Transactional
    public BookingRoom addRoomToBooking(BookingRoom bookingRoom) {
        if (bookingRoom == null ||
                bookingRoom.getRoom() == null ||
                bookingRoom.getBooking() == null) {
            throw new IllegalArgumentException(
                    "Room and booking are required"
            );
        }

        Integer roomId = bookingRoom.getRoom().getRoomId();

        Room lockedRoom = roomRepository.findByIdForUpdate(roomId)
                .orElseThrow(() ->
                        new RuntimeException("Room not found"));

        // Use the locked database entity.
        bookingRoom.setRoom(lockedRoom);

        if (!isRoomAvailable(
                lockedRoom,
                bookingRoom.getBooking()
        )) {
            throw new RuntimeException(
                    "Room is already booked for the selected dates"
            );
        }

        return bookingRoomRepository.save(bookingRoom);
    }

    public List<BookingRoom> getRoomsByBooking(int bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new RuntimeException("Booking not found"));

        return bookingRoomRepository.findByBooking(booking);
    }

    public List<BookingRoom> getAll() {
        return bookingRoomRepository.findAll();
    }

    @Transactional
    public BookingRoom update(
            int bookingRoomId,
            BookingRoom updated
    ) {
        BookingRoom existing =
                bookingRoomRepository.findById(bookingRoomId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Booking room not found"
                                ));

        existing.setPricePerNight(
                updated.getPricePerNight()
        );

        return bookingRoomRepository.save(existing);
    }

    @Transactional
    public void removeRoom(int bookingRoomId) {
        if (!bookingRoomRepository.existsById(bookingRoomId)) {
            throw new RuntimeException(
                    "Booking room not found"
            );
        }

        bookingRoomRepository.deleteById(bookingRoomId);
    }

    public BookingRoom getById(int bookingRoomId) {
        return bookingRoomRepository.findById(bookingRoomId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Booking room not found"
                        ));
    }
}