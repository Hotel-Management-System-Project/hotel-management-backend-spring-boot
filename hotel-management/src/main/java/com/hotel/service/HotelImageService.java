package com.hotel.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hotel.model.HotelImage;
import com.hotel.repository.HotelImageRepository;

@Service
public class HotelImageService {

    private final HotelImageRepository repo;

    public HotelImageService(HotelImageRepository repo) {
        this.repo = repo;
    }

    public HotelImage add(HotelImage img) {
        return repo.save(img);
    }

    public List<HotelImage> getByHotel(Integer hotelId) {
        return repo.findByHotel_HotelId(hotelId);
    }

	public HotelImage getById(Integer id) {
		// TODO Auto-generated method stub
		  return repo.findById(id).orElse(null);
	}

	public void delete(Integer id) {
		  repo.deleteById(id);
		
	}
}