package com.hotel.service;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hotel.dto.RoomRequestDTO;
import com.hotel.dto.RoomResponseDTO;
import com.hotel.model.Room;
import com.hotel.repository.RoomRepoRepository;

import jakarta.transaction.Transactional;

@Transactional
@Service
public class RoomService {
	@Autowired
	private ModelMapper modelMapper;
	@Autowired
	private RoomRepoRepository roomRepoRepository;

	
	
	//add rooms 
	public RoomResponseDTO addRoom(RoomRequestDTO dto) {
		Room room = modelMapper.map(dto, Room.class);
		Room saveRoom = roomRepoRepository.save(room);
		RoomResponseDTO rrdto = modelMapper.map(saveRoom, RoomResponseDTO.class);
		return rrdto;
	}

	
	
	//Get All Rooms
	public List<RoomResponseDTO> getAllRooms() {
		List<Room> rooms = roomRepoRepository.findAll();
		return rooms.stream().map(room -> modelMapper.map(room, RoomResponseDTO.class)).collect(Collectors.toList());
	}

	
	
	
	//delete by room Number 
	public RoomResponseDTO deleteByRoomNumber(Integer roomNumber) {
		 Room room = roomRepoRepository.findByRoomNumber(roomNumber)
		            .orElseThrow(() -> new RuntimeException("Room not found with room number: " + roomNumber));

		    RoomResponseDTO response = modelMapper.map(room, RoomResponseDTO.class);

		    roomRepoRepository.delete(room);

		    return response;
	}
	
	
	//find by room Id
	public RoomResponseDTO findRoomById(int id) {

	    Room room = roomRepoRepository.findById(id)
	            .orElseThrow(() ->
	                new RuntimeException("Room not found with id: " + id));

	    return modelMapper.map(room, RoomResponseDTO.class);
	}
 
	
	
	
	
	// Find room by room type
	public List<RoomResponseDTO> findRoomByType(String roomType) {

	    List<Room> rooms = roomRepoRepository.findByRoomType(roomType);

	    if (rooms.isEmpty()) {
	        throw new RuntimeException("No rooms found with room type: " + roomType);
	    }

	    return rooms.stream()
	            .map(room -> modelMapper.map(room, RoomResponseDTO.class))
	            .collect(Collectors.toList());
	}	
	
	
	// update room by Room Number
	public RoomResponseDTO updateRoomByRoomNumber(Integer roomNumber, RoomRequestDTO dto) {

	    Room room = roomRepoRepository.findByRoomNumber(roomNumber)
	            .orElseThrow(() ->
	                    new RuntimeException("Room not found with room number: " + roomNumber));

	    room.setRoomType(dto.getRoomType());
	    room.setCapacity(dto.getCapacity());
	    room.setPricePerNight(dto.getPricePerNight());
	    room.setAvailabilityStatus(dto.isAvailabilityStatus());

	    Room updatedRoom = roomRepoRepository.save(room);

	    return modelMapper.map(updatedRoom, RoomResponseDTO.class);
	}
	
	
	//Update room by id 
	public RoomResponseDTO updateRoomById(Integer id, RoomRequestDTO dto) {

	    Room room = roomRepoRepository.findById(id)
	            .orElseThrow(() ->
	                    new RuntimeException("Room not found with id: " + id));

	    room.setRoomNumber(dto.getRoomNumber());
	    room.setRoomType(dto.getRoomType());
	    room.setPricePerNight(dto.getPricePerNight());
	    room.setCapacity(dto.getCapacity());
	    room.setAvailabilityStatus(dto.isAvailabilityStatus());

	    Room updatedRoom = roomRepoRepository.save(room);

	    return modelMapper.map(updatedRoom, RoomResponseDTO.class);
	}
	
	
	//delete by room Id
	public RoomResponseDTO deleteRoomById(Integer id) {

	    Room room = roomRepoRepository.findById(id)
	            .orElseThrow(() ->
	                    new RuntimeException("Room not found with id: " + id));

	    roomRepoRepository.delete(room);

	    return modelMapper.map(room, RoomResponseDTO.class);
	}
	
	// Find room by room number
	public RoomResponseDTO findRoomByRoomNumber(Integer roomNumber) {

	    Room room = roomRepoRepository.findByRoomNumber(roomNumber)
	            .orElseThrow(() ->
	                    new RuntimeException("Room not found with room number: " + roomNumber));

	    return modelMapper.map(room, RoomResponseDTO.class);
	}

	
	
}
