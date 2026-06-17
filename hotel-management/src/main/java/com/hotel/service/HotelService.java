package com.hotel.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.hotel.dto.HotelRequest;
import com.hotel.dto.HotelResponse;
import com.hotel.model.Hotel;
import com.hotel.model.Hotel.HotelStatus;
import com.hotel.repository.HotelRepository;

@Service
public class HotelService {

    private final HotelRepository hotelRepository;

    public HotelService(HotelRepository hotelRepository) {
        this.hotelRepository = hotelRepository;
    }

    public HotelResponse createHotel(HotelRequest request) {

        Hotel hotel = new Hotel();

        hotel.setOwnerId(request.getOwnerId());
        hotel.setHotelName(request.getHotelName());
        hotel.setDescription(request.getDescription());
        hotel.setAddress(request.getAddress());
        hotel.setCity(request.getCity());
        hotel.setState(request.getState());
        hotel.setPincode(request.getPincode());
        hotel.setRating(request.getRating());
        hotel.setStatus(HotelStatus.pending);

        Hotel savedHotel = hotelRepository.save(hotel);

        return convertToResponse(savedHotel);
    }

    public List<HotelResponse> getAllHotels() {

        List<HotelResponse> responses = new ArrayList<>();

        for (Hotel hotel : hotelRepository.findAll()) {
            responses.add(convertToResponse(hotel));
        }

        return responses;
    }

    public HotelResponse getHotelById(Long id) {

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hotel not found"));

        return convertToResponse(hotel);
    }

    public HotelResponse updateHotel(Long id, HotelRequest request) {

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hotel not found"));

        hotel.setHotelName(request.getHotelName());
        hotel.setDescription(request.getDescription());
        hotel.setAddress(request.getAddress());
        hotel.setCity(request.getCity());
        hotel.setState(request.getState());
        hotel.setPincode(request.getPincode());
        hotel.setRating(request.getRating());

        return convertToResponse(hotelRepository.save(hotel));
    }

    public void deleteHotel(Long id) {
        hotelRepository.deleteById(id);
    }

    public HotelResponse approveHotel(Long id) {

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hotel not found"));

        hotel.setStatus(HotelStatus.approved);

        return convertToResponse(hotelRepository.save(hotel));
    }

    public HotelResponse rejectHotel(Long id) {

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hotel not found"));

        hotel.setStatus(HotelStatus.rejected);

        return convertToResponse(hotelRepository.save(hotel));
    }

    private HotelResponse convertToResponse(Hotel hotel) {

        HotelResponse response = new HotelResponse();

        response.setHotelId(hotel.getHotelId());
        response.setOwnerId(hotel.getOwnerId());
        response.setHotelName(hotel.getHotelName());
        response.setDescription(hotel.getDescription());
        response.setAddress(hotel.getAddress());
        response.setCity(hotel.getCity());
        response.setState(hotel.getState());
        response.setPincode(hotel.getPincode());
        response.setRating(hotel.getRating());

        if (hotel.getStatus() != null) {
            response.setStatus(hotel.getStatus().name());
        }

        response.setCreatedAt(hotel.getCreatedAt());

        return response;
    }
}