package com.hotel.service;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hotel.dto.RoomRequestDTO;
import com.hotel.dto.RoomResponseDTO;
import com.hotel.model.Hotel;
import com.hotel.model.Room;
import com.hotel.repository.RoomRepoRepository;

@Service
public class RoomService {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private RoomRepoRepository roomRepoRepository;

    // ✅ ADD ROOM
    public RoomResponseDTO addRoom(RoomRequestDTO dto) {

        Room room = new Room();

        room.setRoomNumber(dto.getRoomNumber());
        room.setRoomType(dto.getRoomType());
        room.setPricePerNight(dto.getPricePerNight());
        room.setCapacity(dto.getCapacity());

        room.setAvailabilityStatus(
                Boolean.TRUE.equals(dto.getAvailabilityStatus())
        );

        Hotel hotel = new Hotel();
        hotel.setHotelId(dto.getHotelId());
        room.setHotel(hotel);

        Room saved = roomRepoRepository.save(room);

        return modelMapper.map(saved, RoomResponseDTO.class);
    }

    // ✅ GET ALL
    public List<RoomResponseDTO> getAllRooms() {
        return roomRepoRepository.findAll()
                .stream()
                .map(r -> modelMapper.map(r, RoomResponseDTO.class))
                .collect(Collectors.toList());
    }

    // ✅ DELETE BY ROOM NUMBER
    public RoomResponseDTO deleteByRoomNumber(Integer roomNumber) {

        Room room = roomRepoRepository.findByRoomNumber(roomNumber)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        roomRepoRepository.delete(room);

        return modelMapper.map(room, RoomResponseDTO.class);
    }

    // ✅ GET BY ID
    public RoomResponseDTO findRoomById(Integer id) {

        Room room = roomRepoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        return modelMapper.map(room, RoomResponseDTO.class);
    }

    // ✅ GET ENTITY (IMPORTANT FOR BOOKING)
    public Room getRoomEntityById(int id) {
        return roomRepoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));
    }

    // ✅ FIND BY TYPE
    public List<RoomResponseDTO> findRoomByType(String roomType) {

        return roomRepoRepository.findByRoomType(roomType)
                .stream()
                .map(r -> modelMapper.map(r, RoomResponseDTO.class))
                .collect(Collectors.toList());
    }

    // ✅ UPDATE BY ROOM NUMBER
    public RoomResponseDTO updateRoomByRoomNumber(Integer roomNumber, RoomRequestDTO dto) {

        Room room = roomRepoRepository.findByRoomNumber(roomNumber)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        room.setRoomType(dto.getRoomType());
        room.setCapacity(dto.getCapacity());
        room.setPricePerNight(dto.getPricePerNight());
        room.setAvailabilityStatus(Boolean.TRUE.equals(dto.getAvailabilityStatus()));

        return modelMapper.map(roomRepoRepository.save(room), RoomResponseDTO.class);
    }

    // ✅ UPDATE BY ID
    public RoomResponseDTO updateRoomById(Integer id, RoomRequestDTO dto) {

        Room room = roomRepoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        room.setRoomNumber(dto.getRoomNumber());
        room.setRoomType(dto.getRoomType());
        room.setPricePerNight(dto.getPricePerNight());
        room.setCapacity(dto.getCapacity());
        room.setAvailabilityStatus(Boolean.TRUE.equals(dto.getAvailabilityStatus()));

        return modelMapper.map(roomRepoRepository.save(room), RoomResponseDTO.class);
    }

    // ✅ DELETE BY ID
    public RoomResponseDTO deleteRoomById(Integer id) {

        Room room = roomRepoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        roomRepoRepository.delete(room);

        return modelMapper.map(room, RoomResponseDTO.class);
    }

    // ✅ FIND BY ROOM NUMBER
    public RoomResponseDTO findRoomByRoomNumber(Integer roomNumber) {

        Room room = roomRepoRepository.findByRoomNumber(roomNumber)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        return modelMapper.map(room, RoomResponseDTO.class);
    }
}