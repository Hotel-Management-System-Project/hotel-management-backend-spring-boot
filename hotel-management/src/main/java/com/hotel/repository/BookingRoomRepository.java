package com.hotel.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hotel.model.BookingRoom;

@Repository
public interface BookingRoomRepository extends JpaRepository<BookingRoom, Integer> {
	List<BookingRoom> findByBookingId(int bookingId);
	
	 @Query("""
		        SELECT br FROM BookingRoom br
		        JOIN Booking b ON br.bookingId = b.bookingId
		        WHERE br.roomId = :roomId
		        AND b.checkInDate < :checkOut
		        AND b.checkOutDate > :checkIn
		    """)
		    List<BookingRoom> findConflictingBookings(
		            @Param("roomId") int roomId,
		            @Param("checkIn") LocalDate checkIn,
		            @Param("checkOut") LocalDate checkOut
		    );
}

