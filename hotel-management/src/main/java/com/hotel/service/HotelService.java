package com.hotel.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hotel.model.Hotel;
import com.hotel.repository.HotelRepository;

@Service
public class HotelService {

    private final HotelRepository repo;

    public HotelService(HotelRepository repo) {
        this.repo = repo;
    }

    public Hotel addHotel(Hotel hotel) {
        hotel.setStatus(Hotel.Status.PENDING);
        return repo.save(hotel);
    }

    public List<Hotel> getAll() {
        return repo.findAll();
    }

    public List<Hotel> getByOwner(Integer ownerId) {
        return repo.findByOwner_UserId(ownerId);
    }

    public Hotel approve(Integer id) {
        Hotel h = repo.findById(id).orElseThrow();
        h.setStatus(Hotel.Status.APPROVED);
        return repo.save(h);
    }

    public Hotel reject(Integer id) {
        Hotel h = repo.findById(id).orElseThrow();
        h.setStatus(Hotel.Status.REJECTED);
        return repo.save(h);
    }
}