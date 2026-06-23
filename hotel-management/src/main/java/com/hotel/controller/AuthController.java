package com.hotel.controller;

import java.util.List;
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
<<<<<<< Updated upstream
=======
   
    
    //chenge-pass
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

    //delete
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
    
//users
    @GetMapping("/users")
    public Resp<?> getAllUsers() {
        List<User> users = service.getAllUsers();

        users.forEach(user -> user.setPassword(null));

        return Resp.success(users);
    }
    
    
>>>>>>> Stashed changes
}