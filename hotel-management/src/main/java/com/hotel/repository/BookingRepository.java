package com.hotel.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hotel.model.Booking;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {

    // ✅ FIXED (use relation)
    List<Booking> findByUser_UserId(Integer userId);

    List<Booking> findByCheckInDateBetween(LocalDate from, LocalDate to);
}