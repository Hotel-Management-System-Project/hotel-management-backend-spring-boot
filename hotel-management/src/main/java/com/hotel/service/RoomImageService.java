package com.hotel.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hotel.model.RoomImage;
import com.hotel.repository.RoomImageRepository;

@Service
public class RoomImageService {

    private final RoomImageRepository repository;

    public RoomImageService(
            RoomImageRepository repository
    ) {
        this.repository = repository;
    }

    public RoomImage save(RoomImage image) {
        return repository.save(image);
    }

    public List<RoomImage> getByRoom(Integer roomId) {
        return repository
                .findByRoom_RoomIdOrderByImageIdAsc(roomId);
    }

    public RoomImage getById(Integer imageId) {
        return repository.findById(imageId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Room image not found"
                        )
                );
    }

    public void delete(Integer imageId) {
        repository.deleteById(imageId);
    }
}