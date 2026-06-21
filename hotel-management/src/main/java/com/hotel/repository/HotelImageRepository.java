package com.hotel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hotel.model.HotelImage;

public interface HotelImageRepository extends JpaRepository<HotelImage, Integer> {
   
    List<HotelImage> findByHotel_HotelId(Integer hotelId);

}
