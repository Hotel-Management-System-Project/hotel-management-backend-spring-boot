package com.hotel.controller;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.hotel.config.JwtUtil;
import com.hotel.model.User;
import com.hotel.service.UserService;
import com.hotel.utils.Resp;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
public class AuthController {

    private final UserService service;
    private final JwtUtil jwtUtil;

    public AuthController(UserService service, JwtUtil jwtUtil) {
        this.service = service;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/signup")
    public Resp<?> signup(@RequestBody User user) {
        User saved = service.signup(user);
        saved.setPassword(null);
        return Resp.success(saved);
    }

    @PostMapping("/login")
    public Resp<?> login(@RequestBody Map<String, String> req) {

        User user = service.findByEmail(req.get("email"))
                .orElseThrow(() -> new RuntimeException("Invalid Credentials"));

        if (!service.matchPassword(req.get("password"), user.getPassword())) {
            return Resp.error("Invalid Credentials");
        }

        String token = jwtUtil.generateToken(
                user.getUserId(),
                user.getEmail(),
                user.getRole().name()
        );

        return Resp.success(Map.of(
                "token", token,
                "role", user.getRole(),
                "email", user.getEmail()
        ));
    }

    @PutMapping("/change-password")
    public Resp<?> changePassword(Authentication authentication,
                                  @RequestBody Map<String, String> req) {

        if (authentication == null) {
            return Resp.error("Unauthorized");
        }

        String oldPassword = req.get("oldPassword");
        String newPassword = req.get("newPassword");

        if (oldPassword == null || newPassword == null || newPassword.isBlank()) {
            return Resp.error("Password is required");
        }

        String email = authentication.getName();

        User user = service.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!service.matchPassword(oldPassword, user.getPassword())) {
            return Resp.error("Old password is incorrect");
        }

        user.setPassword(service.encodePassword(newPassword));
        service.save(user);

        return Resp.success("Password changed successfully");
    }

    @DeleteMapping("/delete-account")
    public Resp<?> deleteAccount(Authentication authentication,
                                 @RequestBody Map<String, String> req) {

        if (authentication == null) {
            return Resp.error("Unauthorized");
        }

        String password = req.get("password");

        if (password == null || password.isBlank()) {
            return Resp.error("Password is required");
        }

        String email = authentication.getName();

        User user = service.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!service.matchPassword(password, user.getPassword())) {
            return Resp.error("Password is incorrect");
        }

        service.deleteUser(user.getUserId());

        return Resp.success("Account deleted successfully");
    }

    @GetMapping("/users")
    public Resp<?> getAllUsers() {
        List<User> users = service.getAllUsers();

        users.forEach(user -> user.setPassword(null));

        return Resp.success(users);
    }
}