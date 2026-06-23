package com.hotel.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hotel.model.Room;

public interface RoomRepoRepository extends JpaRepository<Room, Integer> {
	Optional<Room> findByRoomNumber(Integer roomNumber);
  void deleteByRoomNumber(Integer roomNumber);
  List<Room> findByRoomType(String roomType);
  
}