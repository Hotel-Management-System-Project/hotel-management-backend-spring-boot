package com.hotel.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.hotel.model.Booking;
import com.hotel.model.BookingRoom;
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



	  public boolean isRoomAvailable(int roomId, int bookingId) {

	        Booking booking = bookingRepo.findById(bookingId)
	                .orElseThrow(() -> new RuntimeException("Booking not found"));

	        LocalDate checkIn = booking.getCheckInDate();
	        LocalDate checkOut = booking.getCheckOutDate();

	        List<BookingRoom> conflicts =
	                repo.findConflictingBookings(roomId, checkIn, checkOut);

	        return conflicts.isEmpty();
	    }
	  
	  public BookingRoom addRoomToBooking(BookingRoom br) {

	        boolean available = isRoomAvailable(br.getRoomId(), br.getBookingId());

	        if (!available) {
	            throw new RuntimeException("Room already booked for selected dates");
	        }

	        return repo.save(br);
	    }
	  
	  
	  
	    public List<BookingRoom> getRoomsByBooking(int bookingId) {
	        return repo.findByBookingId(bookingId);
	    }

	    
	    public List<BookingRoom> getAll() {
	        return repo.findAll();
	    }
	    
	    

	    public BookingRoom update(int id, BookingRoom updated) {

	        BookingRoom br = repo.findById(id)
	                .orElseThrow(() -> new RuntimeException("Not found"));

	        br.setPricePerNight(updated.getPricePerNight());

	        return repo.save(br);
	    }
	    
	    
	    public void removeRoom(int id) {
	        repo.deleteById(id);
	    }
	    
	    
}
