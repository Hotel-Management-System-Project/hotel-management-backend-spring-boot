package com.hotel.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.hotel.model.Role;
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
        if (repo.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        user.setPassword(encoder.encode(user.getPassword()));

        if (user.getRole() == null) {
            user.setRole(Role.CUSTOMER);
        }

        return repo.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return repo.findByEmail(email);
    }

    public Optional<User> findById(Integer id) {
        return repo.findById(id);
    }

    public boolean matchPassword(String raw, String encoded) {
        return encoder.matches(raw, encoded);
    }

    public String encodePassword(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    public User save(User user) {
        return repo.save(user);
    }

    public List<User> getAllUsers() {
        return repo.findAll();
    }

    public void deleteUser(Integer id) {
        repo.deleteById(id);
    }
}