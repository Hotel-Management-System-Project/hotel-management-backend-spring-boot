package com.hotel.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hotel.config.JwtUtil;
import com.hotel.model.User;
import com.hotel.service.UserService;

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
    public User signup(@RequestBody User user) {
        return service.signup(user);
    }

    @PostMapping("/login")
    public Object login(@RequestBody Map<String, String> req) {

        Optional<User> userOpt = service.findByEmail(req.get("email"));

        if (userOpt.isEmpty()) 
        	return "Invalid";

        User user = userOpt.get();

        if (!service.matchPassword(req.get("password"), user.getPassword())) {
            return "Invalid";
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return Map.of(
                "token", token,
                "role", user.getRole(),
                "email", user.getEmail()
        );
    }
}