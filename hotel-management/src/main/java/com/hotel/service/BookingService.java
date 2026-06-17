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
		super();
		this.repo = repo;
	}


	public Booking createBooking(Booking booking) {
		booking.setStatus(Booking.Status.BOOKED);
		
		return repo.save(booking);
	}
	

	public List<Booking> getUserBookings(int userId){
		return repo.findByUserId(userId);	
	}
	
	 public List<Booking> getAllBookings() {
	        return repo.findAll();
	    }
	 
	 
	 
	 public Booking getById(int id) {
	        return repo.findById(id)
	                .orElseThrow(() -> new RuntimeException("Booking Not Found"));
	    }

	 
	 
	  public Booking updateBooking(int id, Booking updated) {
	        Booking existing = getById(id);

	        existing.setCheckInDate(updated.getCheckInDate());
	        existing.setCheckOutDate(updated.getCheckOutDate());
	        existing.setTotalAmount(updated.getTotalAmount());

	        return repo.save(existing);
	    }
	  
	  
	 
	public void cancelBooking(int userId) {
		Booking booking = repo.findById(userId).orElseThrow(()-> new RuntimeException("Booking Not Found"));
		
		booking.setStatus(Booking.Status.CANCELLED);
		repo.save(booking);
	}
	
	 public void deleteBooking(int id) {
	        repo.deleteById(id);
	    }

	 
	 public List<Booking> searchByDate(LocalDate from, LocalDate to) {
	        return repo.findByCheckInDateBetween(from, to);
	    }
	 
}
