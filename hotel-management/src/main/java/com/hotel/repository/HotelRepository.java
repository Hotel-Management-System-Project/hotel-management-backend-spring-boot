package com.hotel.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.hotel.model.Hotel;

public interface HotelRepository extends JpaRepository<Hotel, Integer> {
	
    List<Hotel> findByOwner_UserId(Integer ownerId);

}