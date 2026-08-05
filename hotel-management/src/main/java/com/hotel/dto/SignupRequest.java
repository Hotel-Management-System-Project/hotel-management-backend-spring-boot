package com.hotel.dto;

import com.hotel.model.Role;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SignupRequest {

    private String fullName;
    private String email;
    private String phone;
    private String password;
    private Role role;
    private String emailVerificationToken;
}