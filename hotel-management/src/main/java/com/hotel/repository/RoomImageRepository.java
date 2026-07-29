package com.hotel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hotel.model.RoomImage;

public interface RoomImageRepository
        extends JpaRepository<RoomImage, Integer> {

    List<RoomImage> findByRoom_RoomIdOrderByImageIdAsc(
            Integer roomId
    );
}

