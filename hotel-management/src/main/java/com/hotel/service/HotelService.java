package com.hotel.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hotel.dto.HotelRequest;
import com.hotel.model.Hotel;
import com.hotel.model.User;
import com.hotel.repository.HotelRepository;
import com.hotel.repository.UserRepository;

@Service
public class HotelService {

    private final HotelRepository repo;
    private final UserRepository userRepository;

    public HotelService(HotelRepository repo,
                        UserRepository userRepository) {
        this.repo = repo;
        this.userRepository = userRepository;
    }

    public Hotel addHotel(HotelRequest request) {

        User owner = userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        Hotel hotel = new Hotel();

        hotel.setOwner(owner);
        hotel.setHotelName(request.getHotelName());
        hotel.setDescription(request.getDescription());
        hotel.setAddress(request.getAddress());
        hotel.setCity(request.getCity());
        hotel.setState(request.getState());
        hotel.setPincode(request.getPincode());

        if (request.getRating() != null) {
            hotel.setRating(request.getRating().doubleValue());
        }

        hotel.setStatus(Hotel.Status.PENDING);

        return repo.save(hotel);
    }

    public List<Hotel> getAll() {
        return repo.findAll();
    }

    public List<Hotel> getByOwner(Integer ownerId) {
        return repo.findByOwner_UserId(ownerId);
    }
    
    public Hotel getById(Integer id) {
        return repo.findById(id)
                .orElseThrow(() ->
                    new RuntimeException("Hotel not found"));
    }

    public Hotel approve(Integer id) {
        Hotel hotel = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Hotel not found"));

        hotel.setStatus(Hotel.Status.APPROVED);

        return repo.save(hotel);
    }

    public Hotel reject(Integer id) {
        Hotel hotel = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Hotel not found"));

        hotel.setStatus(Hotel.Status.REJECTED);

        return repo.save(hotel);
    }
}