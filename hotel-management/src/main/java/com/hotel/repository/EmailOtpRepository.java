package com.hotel.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hotel.model.EmailOtp;

public interface EmailOtpRepository extends JpaRepository<EmailOtp, Long> {
    Optional<EmailOtp> findTopByEmailOrderByIdDesc(String email);
    Optional<EmailOtp> findByEmailAndVerificationToken(
            String email,
            String verificationToken
    );
    void deleteByEmail(String email);
}
