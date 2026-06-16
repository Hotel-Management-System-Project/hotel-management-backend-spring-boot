package com.hotel.service;

import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.hotel.model.User;
import com.hotel.repository.UserRepository;



@Service
public class UserService {

    private final UserRepository repo;
    private final BCryptPasswordEncoder encoder;

    public UserService(UserRepository repo, BCryptPasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    public User signup(User user) {
        user.setPassword(encoder.encode(user.getPassword()));
    
        if (user.getRole() == null) {
            user.setRole(User.Role.CUSTOMER);
        }
        return repo.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return repo.findByEmail(email);
    }

    public boolean matchPassword(String raw, String encoded) {
        return encoder.matches(raw, encoded);
    }
}