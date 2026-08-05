package com.hotel.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hotel.model.Room;

import jakarta.persistence.LockModeType;

public interface RoomRepoRepository
        extends JpaRepository<Room, Integer> {

    Optional<Room> findByRoomNumber(Integer roomNumber);

    void deleteByRoomNumber(Integer roomNumber);

    List<Room> findByRoomType(String roomType);

    boolean existsByHotel_HotelIdAndRoomNumber(
            Integer hotelId,
            Integer roomNumber
    );

    /*
     * Locks the selected room until the current booking transaction finishes.
     * This prevents two customers from booking the same room simultaneously.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT r
        FROM Room r
        WHERE r.roomId = :roomId
        """)
    Optional<Room> findByIdForUpdate(
            @Param("roomId") Integer roomId
    );
}