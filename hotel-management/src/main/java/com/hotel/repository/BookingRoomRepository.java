package com.hotel.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.hotel.model.Booking;
import com.hotel.model.BookingRoom;
import com.hotel.model.Room;
import org.springframework.data.repository.query.Param;


public interface BookingRoomRepository extends JpaRepository<BookingRoom, Integer> {

    List<BookingRoom> findByBooking(Booking booking);

    @Query("""
    	    SELECT br FROM BookingRoom br
    	    WHERE br.room = :room
    	    AND br.booking.checkInDate < :checkOut
    	    AND br.booking.checkOutDate > :checkIn
    	""")
    	List<BookingRoom> findConflictingBookings(
    	        @Param("room") Room room,
    	        @Param("checkIn") LocalDate checkIn,
    	        @Param("checkOut") LocalDate checkOut
    	);
}