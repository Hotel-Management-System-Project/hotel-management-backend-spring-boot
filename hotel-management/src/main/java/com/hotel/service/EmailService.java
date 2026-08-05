package com.hotel.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.hotel.model.User;

/**
 * Sends signup verification codes and welcome emails.
 */
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:no-reply@stayflow.local}")
    private String senderEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Sends an OTP when a hotel owner verifies their email.
     */
    public void sendSignupOtp(String email, String code) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(senderEmail);
        message.setTo(email);
        message.setSubject("Your StayFlow verification code");
        message.setText(
                "Your StayFlow hotel-owner signup verification code is:\n\n"
                        + code
                        + "\n\nThis code expires in 10 minutes. "
                        + "Do not share it with anyone."
        );

        mailSender.send(message);
    }

    /**
     * Sends a welcome message after successful registration.
     */
    public void sendWelcomeEmail(User user) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(senderEmail);
        message.setTo(user.getEmail());
        message.setSubject("Welcome to StayFlow");
        message.setText(
                "Hello " + user.getFullName() + ",\n\n"
                        + "Your email has been verified successfully. "
                        + "Welcome to StayFlow!\n\n"
                        + "You can now sign in, register your hotel, add rooms, "
                        + "and submit your property for administrator approval."
        );

        mailSender.send(message);
    }
    
    public void sendPasswordResetOtp(String email, String code) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(senderEmail);
        message.setTo(email);
        message.setSubject("Reset your StayFlow password");
        message.setText(
                "Your StayFlow password reset code is:\n\n"
                + code
                + "\n\nThis code expires in 10 minutes."
                + "\nIf you did not request a password reset, ignore this email."
        );

        mailSender.send(message);
    }
}