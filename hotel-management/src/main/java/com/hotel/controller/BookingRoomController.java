package com.hotel.controller;

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

import com.hotel.dto.BookingRoomDTO;
import com.hotel.model.BookingRoom;
import com.hotel.service.BookingRoomService;
import com.hotel.utils.Resp;




@RestController
@RequestMapping("/api/booking-rooms")
@CrossOrigin("*")
public class BookingRoomController {

	
	 private final BookingRoomService service;
	    private final ModelMapper mapper;

	    public BookingRoomController(BookingRoomService service, ModelMapper mapper) {
	        this.service = service;
	        this.mapper = mapper;
	    }
	
	    @PostMapping
	    public Resp<BookingRoomDTO> add(@RequestBody BookingRoomDTO dto) {

	        BookingRoom br = mapper.map(dto, BookingRoom.class);

	        return Resp.success(
	                mapper.map(service.addRoomToBooking(br), BookingRoomDTO.class)
	        );
	    }
	    
	    
	    @GetMapping("/check")
	    public Resp<Boolean> check(@RequestParam int roomId,
	                               @RequestParam int bookingId) {

	        return Resp.success(service.isRoomAvailable(roomId, bookingId));
	    }

	    
	    @GetMapping("/{bookingId}")
	    public Resp<List<BookingRoomDTO>> getByBooking(@PathVariable int bookingId) {

	        List<BookingRoomDTO> list = service.getRoomsByBooking(bookingId)
	                .stream()
	                .map(b -> mapper.map(b, BookingRoomDTO.class))
	                .collect(Collectors.toList());

	        return Resp.success(list);
	    }
	    
	    
	    
	    

	    @GetMapping
	    public Resp<List<BookingRoomDTO>> getAll() {

	        List<BookingRoomDTO> list = service.getAll()
	                .stream()
	                .map(b -> mapper.map(b, BookingRoomDTO.class))
	                .collect(Collectors.toList());

	        return Resp.success(list);
	    }
	
	    
	    @PutMapping("/{id}")
	    public Resp<BookingRoomDTO> update(@PathVariable int id,
	                                       @RequestBody BookingRoomDTO dto) {

	        BookingRoom br = mapper.map(dto, BookingRoom.class);

	        return Resp.success(
	                mapper.map(service.update(id, br), BookingRoomDTO.class)
	        );
	    }
	    
	    @DeleteMapping("/{id}")
	    public Resp<String> delete(@PathVariable int id) {

	        service.removeRoom(id);
	        return Resp.success("Room removed from booking");
	    }
}
