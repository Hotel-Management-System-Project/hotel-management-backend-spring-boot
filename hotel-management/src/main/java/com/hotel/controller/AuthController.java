package com.hotel.controller;

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hotel.config.JwtUtil;
import com.hotel.dto.SignupRequest;
import com.hotel.model.Role;
import com.hotel.model.User;
import com.hotel.service.UserService;
import com.hotel.utils.Resp;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthController(
            UserService userService,
            JwtUtil jwtUtil) {

        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Registers a customer or verified hotel owner.
     *
     * SignupRequest is used instead of the User entity to prevent
     * recursive JSON conversion through bookings, rooms, and hotels.
     */
    @PostMapping("/signup")
    public Resp<?> signup(
            @RequestBody SignupRequest request) {

        if (request == null) {
            return Resp.error(
                    "Signup information is required"
            );
        }

        User user = new User();

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(request.getPassword());
        // Public registration must never be able to create an administrator.
        // Hotel owners still complete the existing OTP verification flow.
        user.setRole(request.getRole() == Role.HOTEL_OWNER
                ? Role.HOTEL_OWNER
                : Role.CUSTOMER);
        user.setEmailVerificationToken(
                request.getEmailVerificationToken()
        );

        User savedUser = userService.signup(user);

        return Resp.success(
                Map.of(
                        "message",
                        "Signup successful. Your account is ready to use.",

                        "email",
                        savedUser.getEmail()
                )
        );
    }

    /**
     * Sends a six-digit signup OTP to the supplied email.
     */
    @PostMapping("/send-signup-otp")
    public Resp<?> sendSignupOtp(
            @RequestBody Map<String, String> request) {

        if (request == null) {
            return Resp.error("Email is required");
        }

        String email = request.get("email");

        if (email == null || email.isBlank()) {
            return Resp.error("Email is required");
        }

        userService.sendSignupOtp(email);

        return Resp.success(
                "Verification code sent successfully"
        );
    }

    /**
     * Verifies the signup OTP and returns a temporary verification token.
     */
    @PostMapping("/verify-signup-otp")
    public Resp<?> verifySignupOtp(
            @RequestBody Map<String, String> request) {

        if (request == null) {
            return Resp.error(
                    "Email and verification code are required"
            );
        }

        String email = request.get("email");
        String code = request.get("code");

        if (email == null || email.isBlank()) {
            return Resp.error("Email is required");
        }

        if (code == null || code.isBlank()) {
            return Resp.error(
                    "Verification code is required"
            );
        }

        String verificationToken =
                userService.verifySignupOtp(email, code);

        return Resp.success(
                Map.of(
                        "message",
                        "Email verified successfully",

                        "verificationToken",
                        verificationToken
                )
        );
    }

    /**
     * Authenticates the user and returns a JWT token.
     */
    @PostMapping("/login")
    public Resp<?> login(
            @RequestBody Map<String, String> request
    ) {
        if (request == null) {
            return Resp.error(
                    "Email and password are required"
            );
        }

        String email = request.get("email");
        String password = request.get("password");

        if (email == null
                || email.isBlank()
                || password == null
                || password.isBlank()) {

            return Resp.error(
                    "Email and password are required"
            );
        }

        email = email.trim().toLowerCase();

        User user = userService
                .findByEmail(email)
                .orElse(null);

        if (user == null) {
            return Resp.error(
                    "Invalid email or password"
            );
        }

        if (!userService.matchPassword(
                password,
                user.getPassword()
        )) {
            return Resp.error(
                    "Invalid email or password"
            );
        }

        if (!Boolean.TRUE.equals(
                user.getActiveStatus()
        )) {
            return Resp.error(
                    "Your account is inactive"
            );
        }

        if (user.getRole() == null) {
            return Resp.error(
                    "No role is assigned to this account"
            );
        }

        String role = user.getRole().name();

        String token = jwtUtil.generateToken(
                user.getUserId(),
                user.getEmail(),
                role
        );

        return Resp.success(
                Map.of(
                        "token", token,
                        "userId", user.getUserId(),
                        "role", role,
                        "email", user.getEmail(),
                        "fullName", user.getFullName()
                )
        );
    }

    /**
     * Changes the password of the authenticated user.
     */
    @PutMapping("/change-password")
    public Resp<?> changePassword(
            Authentication authentication,
            @RequestBody Map<String, String> request) {

        if (authentication == null) {
            return Resp.error("Unauthorized");
        }

        if (request == null) {
            return Resp.error(
                    "Password information is required"
            );
        }

        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");

        if (oldPassword == null
                || oldPassword.isBlank()
                || newPassword == null
                || newPassword.isBlank()) {

            return Resp.error(
                    "Old password and new password are required"
            );
        }

        if (newPassword.length() < 6) {
            return Resp.error(
                    "New password must contain at least 6 characters"
            );
        }

        User user = userService
                .findByEmail(authentication.getName())
                .orElse(null);

        if (user == null) {
            return Resp.error("User not found");
        }

        if (!userService.matchPassword(
                oldPassword,
                user.getPassword())) {

            return Resp.error(
                    "Old password is incorrect"
            );
        }

        user.setPassword(
                userService.encodePassword(newPassword)
        );

        userService.save(user);

        return Resp.success(
                "Password changed successfully"
        );
    }

    /**
     * Deletes the authenticated user's account after password confirmation.
     */
    @DeleteMapping("/delete-account")
    public Resp<?> deleteAccount(
            Authentication authentication,
            @RequestBody Map<String, String> request) {

        if (authentication == null) {
            return Resp.error("Unauthorized");
        }

        if (request == null) {
            return Resp.error("Password is required");
        }

        String password = request.get("password");

        if (password == null || password.isBlank()) {
            return Resp.error("Password is required");
        }

        User user = userService
                .findByEmail(authentication.getName())
                .orElse(null);

        if (user == null) {
            return Resp.error("User not found");
        }

        if (!userService.matchPassword(
                password,
                user.getPassword())) {

            return Resp.error(
                    "Password is incorrect"
            );
        }

        userService.deleteUser(user.getUserId());

        return Resp.success(
                "Account deleted successfully"
        );
    }
    
    @PostMapping("/forgot-password/send-otp")
    public Resp<?> sendPasswordResetOtp(
            @RequestBody Map<String, String> request
    ) {
    	userService.sendPasswordResetOtp(
                request.get("email")
        );

        return Resp.success(
                "Password reset code sent successfully"
        );
    }
    
    @PostMapping("/forgot-password/reset")
    public Resp<?> resetPassword(
            @RequestBody Map<String, String> request
    ) {
    	userService.resetPassword(
                request.get("email"),
                request.get("code"),
                request.get("newPassword")
        );

        return Resp.success(
                "Password reset successfully"
        );
    }
}
