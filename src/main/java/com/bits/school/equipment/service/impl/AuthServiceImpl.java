package com.bits.school.equipment.service.impl;

import com.bits.school.equipment.dto.AuthResponse;
import com.bits.school.equipment.dto.LoginRequest;
import com.bits.school.equipment.dto.SignupRequest;
import com.bits.school.equipment.entity.User;
import com.bits.school.equipment.service.AuthService;
import com.bits.school.equipment.service.UserService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final Map<String, Long> tokenStore = new ConcurrentHashMap<>();

    public AuthServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public AuthResponse signup(SignupRequest request) {
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setRole(User.Role.valueOf(request.getRole().toUpperCase()));

        User created = userService.createUser(user);
        String token = "TOKEN-" + UUID.randomUUID().toString();
        tokenStore.put(token, created.getId());

        return new AuthResponse(token, created.getId(), created.getName(),
                                created.getEmail(), created.getRole().name());
    }

    @Override
    public Optional<AuthResponse> login(LoginRequest request) {
        Optional<User> userOpt = userService.findByEmail(request.getEmail());

        if (userOpt.isEmpty() || !userOpt.get().getPassword().equals(request.getPassword())) {
            return Optional.empty();
        }

        User user = userOpt.get();
        String token = "TOKEN-" + UUID.randomUUID().toString();
        tokenStore.put(token, user.getId());

        return Optional.of(new AuthResponse(token, user.getId(), user.getName(),
                                            user.getEmail(), user.getRole().name()));
    }

    @Override
    public Optional<User> validateToken(String token) {
        if (token == null) return Optional.empty();
        Long userId = tokenStore.get(token);
        if (userId == null) return Optional.empty();
        return userService.findById(userId);
    }

    @Override
    public void logout(String token) {
        if (token != null) tokenStore.remove(token);
    }
}
