package com.hotel.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hotel.dto.BookingDTO;
import com.hotel.model.Booking;
import com.hotel.service.BookingService;
import com.hotel.utils.Resp;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin("*")
public class BookingController {


	 private final BookingService service;
	    private final ModelMapper mapper;

	    public BookingController(BookingService service, ModelMapper mapper) {
	        this.service = service;
	        this.mapper = mapper;
	    }


	    @PostMapping("/user/{userId}")
	    public Resp<BookingDTO> create(@PathVariable int userId,
	                                   @RequestBody BookingDTO dto) {

	        Booking booking = mapper.map(dto, Booking.class);
	        booking.setUserId(userId);

	        Booking saved = service.createBooking(booking);

	        return Resp.success(mapper.map(saved, BookingDTO.class));
	    }
	 
	 
	    @GetMapping("/user/{userId}")
	    public Resp<List<BookingDTO>> getUserBookings(@PathVariable int userId) {

	        List<BookingDTO> list = service.getUserBookings(userId)
	                .stream()
	                .map(b -> mapper.map(b, BookingDTO.class))
	                .collect(Collectors.toList());

	        return Resp.success(list);
	    }
	    
	    
	    @GetMapping
	    public Resp<List<BookingDTO>> getAll() {

	        List<BookingDTO> list = service.getAllBookings()
	                .stream()
	                .map(b -> mapper.map(b, BookingDTO.class))
	                .collect(Collectors.toList());

	        return Resp.success(list);
	    }
	    
	    
	    @GetMapping("/{id}")
	    public Resp<BookingDTO> getById(@PathVariable int id) {
	        return Resp.success(mapper.map(service.getById(id), BookingDTO.class));
	    }
	    
	    
	    
	    
	    @PutMapping("/{id}")
	    public Resp<BookingDTO> update(@PathVariable int id,
	                                   @RequestBody BookingDTO dto) {

	        Booking booking = mapper.map(dto, Booking.class);

	        return Resp.success(
	                mapper.map(service.updateBooking(id, booking), BookingDTO.class)
	        );
	    }
	    
	    
	 
	 
	    @PutMapping("/cancel/{id}")
	    public Resp<String> cancel(@PathVariable int id) {
	        service.cancelBooking(id);
	        return Resp.success("Booking Cancelled Successfully");
	    }
	    
	    
	    @GetMapping("/search")
	    public Resp<List<BookingDTO>> search(
	            @RequestParam String from,
	            @RequestParam String to) {

	        List<BookingDTO> list = service.searchByDate(
	                        LocalDate.parse(from),
	                        LocalDate.parse(to))
	                .stream()
	                .map(b -> mapper.map(b, BookingDTO.class))
	                .collect(Collectors.toList());

	        return Resp.success(list);
	    }
	    
	    
	    
	    @DeleteMapping("/{id}")
	    public Resp<String> delete(@PathVariable int id) {
	        service.deleteBooking(id);
	        return Resp.success("Booking Deleted Successfully");
	    }
	    
	    
	 
}
