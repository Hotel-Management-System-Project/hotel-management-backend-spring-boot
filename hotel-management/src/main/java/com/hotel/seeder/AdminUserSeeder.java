package com.hotel.seeder;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.hotel.model.Role;
import com.hotel.model.User;
import com.hotel.repository.UserRepository;

@Component
public class AdminUserSeeder implements CommandLineRunner {

    private static final String ADMIN_EMAIL = "admin@gmail.com";

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AdminUserSeeder(
            UserRepository userRepository,
            BCryptPasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        if (userRepository.findByEmailIgnoreCase(ADMIN_EMAIL).isPresent()) {
            System.out.println("Default admin already exists. Skipping.");
            return;
        }

        User admin = User.builder()
                .fullName("Hotel Admin")
                .email(ADMIN_EMAIL)
                .password(passwordEncoder.encode("admin"))
                .role(Role.ADMIN)
                .activeStatus(true)
                .emailVerified(true)
                .build();

        userRepository.save(admin);
        System.out.println("Default admin created: " + ADMIN_EMAIL);
    }
}
