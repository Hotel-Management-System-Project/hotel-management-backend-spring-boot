package com.hotel.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotel.dto.HotelRequest;
import com.hotel.model.Hotel;
import com.hotel.model.User;
import com.hotel.repository.HotelRepository;
import com.hotel.repository.UserRepository;

@Service
public class HotelService {

    private final HotelRepository hotelRepository;
    private final UserRepository userRepository;

    public HotelService(
            HotelRepository hotelRepository,
            UserRepository userRepository
    ) {
        this.hotelRepository = hotelRepository;
        this.userRepository = userRepository;
    }

    // Add a new hotel
    public Hotel addHotel(HotelRequest request) {
        if (request.getOwnerId() == null) {
            throw new RuntimeException("Owner id is required");
        }

        User owner = userRepository
                .findById(request.getOwnerId())
                .orElseThrow(() ->
                        new RuntimeException("Owner not found")
                );

        Hotel hotel = new Hotel();

        hotel.setOwner(owner);
        hotel.setHotelName(request.getHotelName());
        hotel.setDescription(request.getDescription());
        hotel.setAddress(request.getAddress());
        hotel.setCity(request.getCity());
        hotel.setState(request.getState());
        hotel.setPincode(request.getPincode());

        if (request.getRating() != null) {
            hotel.setRating(
                    request.getRating().doubleValue()
            );
        } else {
            hotel.setRating(0.0);
        }

        hotel.setStatus(Hotel.Status.PENDING);

        return hotelRepository.save(hotel);
    }

    // Get all hotels
    @Transactional(readOnly = true)
    public List<Hotel> getAll() {
        return hotelRepository.findAll();
    }

    // Get hotels belonging to one owner
    @Transactional(readOnly = true)
    public List<Hotel> getByOwner(Integer ownerId) {
        return hotelRepository
                .findByOwner_UserId(ownerId);
    }

    // Get hotel by ID
    @Transactional(readOnly = true)
    public Hotel getById(Integer hotelId) {
        return hotelRepository
                .findById(hotelId)
                .orElseThrow(() ->
                        new RuntimeException("Hotel not found")
                );
    }

    // Approve hotel
    @Transactional
    public Hotel approve(Integer hotelId) {
        Hotel hotel = getById(hotelId);

        hotel.setStatus(Hotel.Status.APPROVED);

        return hotelRepository.save(hotel);
    }

    // Reject hotel
    @Transactional
    public Hotel reject(Integer hotelId) {
        Hotel hotel = getById(hotelId);

        hotel.setStatus(Hotel.Status.REJECTED);

        return hotelRepository.save(hotel);
    }

    // Delete hotel
    @Transactional
    public void deleteHotel(
            Integer hotelId,
            String requesterEmail,
            boolean admin
    ) {
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(() ->
                        new RuntimeException("Hotel not found")
                );

        if (!admin) {
            if (hotel.getOwner() == null) {
                throw new RuntimeException(
                        "Hotel owner information is missing"
                );
            }

            if (!hotel.getOwner()
                    .getEmail()
                    .equalsIgnoreCase(requesterEmail)) {

                throw new RuntimeException(
                        "You can delete only your own hotel"
                );
            }
        }

        /*
         * Hotel entity should cascade deletion to:
         * - hotel images
         * - rooms
         * - room images
         * - booking-room relationships
         */
        hotelRepository.delete(hotel);
    }
}