package com.hotel.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotel.dto.BulkRoomRequestDTO;
import com.hotel.dto.RoomRequestDTO;
import com.hotel.dto.RoomResponseDTO;
import com.hotel.model.Hotel;
import com.hotel.model.Room;
import com.hotel.repository.HotelRepository;
import com.hotel.repository.RoomRepoRepository;

@Service
public class RoomService {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private RoomRepoRepository roomRepoRepository;

    @Autowired
    private HotelRepository hotelRepository;

    // Applies AC, Wi-Fi and TV selections to a room.
    private void applyAmenities(
            Room room,
            Boolean airConditioned,
            Boolean hasWifi,
            Boolean hasTv
    ) {
        room.setAirConditioned(
                Boolean.TRUE.equals(airConditioned)
        );

        // Wi-Fi is enabled by default.
        room.setHasWifi(
                hasWifi == null || Boolean.TRUE.equals(hasWifi)
        );

        room.setHasTv(
                Boolean.TRUE.equals(hasTv)
        );
    }

    // Add one room.
    public RoomResponseDTO addRoom(RoomRequestDTO dto) {

        Hotel hotel = hotelRepository.findById(dto.getHotelId())
                .orElseThrow(() ->
                        new RuntimeException("Hotel not found")
                );

        boolean roomExists =
                roomRepoRepository
                        .existsByHotel_HotelIdAndRoomNumber(
                                dto.getHotelId(),
                                dto.getRoomNumber()
                        );

        if (roomExists) {
            throw new RuntimeException(
                    "Room number " + dto.getRoomNumber()
                            + " already exists in this hotel"
            );
        }

        Room room = new Room();

        room.setHotel(hotel);
        room.setRoomNumber(dto.getRoomNumber());
        room.setRoomType(dto.getRoomType());
        room.setPricePerNight(dto.getPricePerNight());
        room.setCapacity(dto.getCapacity());

        applyAmenities(
                room,
                dto.getAirConditioned(),
                dto.getHasWifi(),
                dto.getHasTv()
        );

        room.setAvailabilityStatus(
                Boolean.TRUE.equals(
                        dto.getAvailabilityStatus()
                )
        );

        Room savedRoom = roomRepoRepository.save(room);

        return modelMapper.map(
                savedRoom,
                RoomResponseDTO.class
        );
    }

    // Add up to 100 rooms with sequential room numbers.
    @Transactional
    public List<RoomResponseDTO> addRoomsInBulk(
            BulkRoomRequestDTO dto,
            String requesterEmail,
            boolean isAdmin
    ) {
        Hotel hotel = hotelRepository
                .findById(dto.getHotelId())
                .orElseThrow(() ->
                        new RuntimeException("Hotel not found")
                );

        if (!isAdmin
                && (hotel.getOwner() == null
                || !hotel.getOwner()
                        .getEmail()
                        .equalsIgnoreCase(requesterEmail))) {

            throw new RuntimeException(
                    "You can add rooms only to your own hotel"
            );
        }

        List<Room> rooms = new ArrayList<>();

        for (int offset = 0;
             offset < dto.getQuantity();
             offset++) {

            int roomNumber =
                    dto.getStartRoomNumber() + offset;

            boolean roomExists =
                    roomRepoRepository
                            .existsByHotel_HotelIdAndRoomNumber(
                                    dto.getHotelId(),
                                    roomNumber
                            );

            if (roomExists) {
                throw new RuntimeException(
                        "Room number " + roomNumber
                                + " already exists in this hotel"
                );
            }

            Room room = new Room();

            room.setHotel(hotel);
            room.setRoomNumber(roomNumber);
            room.setRoomType(dto.getRoomType());
            room.setPricePerNight(
                    dto.getPricePerNight()
            );
            room.setCapacity(dto.getCapacity());

            applyAmenities(
                    room,
                    dto.getAirConditioned(),
                    dto.getHasWifi(),
                    dto.getHasTv()
            );

            room.setAvailabilityStatus(
                    Boolean.TRUE.equals(
                            dto.getAvailabilityStatus()
                    )
            );

            rooms.add(room);
        }

        return roomRepoRepository.saveAll(rooms)
                .stream()
                .map(room -> modelMapper.map(
                        room,
                        RoomResponseDTO.class
                ))
                .collect(Collectors.toList());
    }

    // Get all rooms.
    public List<RoomResponseDTO> getAllRooms() {

        return roomRepoRepository.findAll()
                .stream()
                .map(room -> modelMapper.map(
                        room,
                        RoomResponseDTO.class
                ))
                .collect(Collectors.toList());
    }

    // Delete room using its room number.
    public RoomResponseDTO deleteByRoomNumber(
            Integer roomNumber
    ) {
        Room room = roomRepoRepository
                .findByRoomNumber(roomNumber)
                .orElseThrow(() ->
                        new RuntimeException("Room not found")
                );

        roomRepoRepository.delete(room);

        return modelMapper.map(
                room,
                RoomResponseDTO.class
        );
    }

    // Find room using database room ID.
    public RoomResponseDTO findRoomById(Integer id) {

        Room room = roomRepoRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Room not found")
                );

        return modelMapper.map(
                room,
                RoomResponseDTO.class
        );
    }

    // Return room entity for booking operations.
    public Room getRoomEntityById(int id) {

        return roomRepoRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Room not found")
                );
    }

    // Find rooms by room type.
    public List<RoomResponseDTO> findRoomByType(
            String roomType
    ) {
        return roomRepoRepository
                .findByRoomType(roomType)
                .stream()
                .map(room -> modelMapper.map(
                        room,
                        RoomResponseDTO.class
                ))
                .collect(Collectors.toList());
    }

    // Update room using its room number.
    public RoomResponseDTO updateRoomByRoomNumber(
            Integer roomNumber,
            RoomRequestDTO dto
    ) {
        Room room = roomRepoRepository
                .findByRoomNumber(roomNumber)
                .orElseThrow(() ->
                        new RuntimeException("Room not found")
                );

        room.setRoomType(dto.getRoomType());
        room.setCapacity(dto.getCapacity());
        room.setPricePerNight(
                dto.getPricePerNight()
        );

        applyAmenities(
                room,
                dto.getAirConditioned(),
                dto.getHasWifi(),
                dto.getHasTv()
        );

        room.setAvailabilityStatus(
                Boolean.TRUE.equals(
                        dto.getAvailabilityStatus()
                )
        );

        Room savedRoom =
                roomRepoRepository.save(room);

        return modelMapper.map(
                savedRoom,
                RoomResponseDTO.class
        );
    }

    // Update room using database room ID.
    public RoomResponseDTO updateRoomById(
            Integer id,
            RoomRequestDTO dto
    ) {
        Room room = roomRepoRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Room not found")
                );

        room.setRoomNumber(dto.getRoomNumber());
        room.setRoomType(dto.getRoomType());
        room.setPricePerNight(
                dto.getPricePerNight()
        );
        room.setCapacity(dto.getCapacity());

        applyAmenities(
                room,
                dto.getAirConditioned(),
                dto.getHasWifi(),
                dto.getHasTv()
        );

        room.setAvailabilityStatus(
                Boolean.TRUE.equals(
                        dto.getAvailabilityStatus()
                )
        );

        Room savedRoom =
                roomRepoRepository.save(room);

        return modelMapper.map(
                savedRoom,
                RoomResponseDTO.class
        );
    }

    // Delete room using database room ID.
    public RoomResponseDTO deleteRoomById(Integer id) {

        Room room = roomRepoRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Room not found")
                );

        roomRepoRepository.delete(room);

        return modelMapper.map(
                room,
                RoomResponseDTO.class
        );
    }

    // Find room using room number.
    public RoomResponseDTO findRoomByRoomNumber(
            Integer roomNumber
    ) {
        Room room = roomRepoRepository
                .findByRoomNumber(roomNumber)
                .orElseThrow(() ->
                        new RuntimeException("Room not found")
                );

        return modelMapper.map(
                room,
                RoomResponseDTO.class
        );
    }
}


