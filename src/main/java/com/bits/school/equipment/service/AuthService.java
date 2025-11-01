package com.bits.school.equipment.service;

import com.bits.school.equipment.dto.AuthResponse;
import com.bits.school.equipment.dto.LoginRequest;
import com.bits.school.equipment.dto.SignupRequest;
import com.bits.school.equipment.entity.User;

import java.util.Optional;

public interface AuthService {
    AuthResponse signup(SignupRequest request);
    Optional<AuthResponse> login(LoginRequest request);
    Optional<User> validateToken(String token);
    void logout(String token);
}

