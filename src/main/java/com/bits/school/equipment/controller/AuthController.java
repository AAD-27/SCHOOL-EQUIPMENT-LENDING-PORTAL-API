package com.bits.school.equipment.controller;

import com.bits.school.equipment.dto.AuthResponse;
import com.bits.school.equipment.dto.LoginRequest;
import com.bits.school.equipment.dto.SignupRequest;
import com.bits.school.equipment.entity.User;
import com.bits.school.equipment.entity.Role;
import com.bits.school.equipment.entity.Category;
import com.bits.school.equipment.service.AuthService;
import com.bits.school.equipment.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173/", allowCredentials = "true")
public class AuthController {
    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    // Public: list all roles for dropdown
    @GetMapping("/roles")
    public ResponseEntity<List<Role>> getRoles() {
        return ResponseEntity.ok(authService.getAllRoles());
    }

    // Public: list all categories for dropdown
    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getCategories() {
        return ResponseEntity.ok(authService.getAllCategories());
    }

    @PostMapping("/signup")
    public ResponseEntity<Object> signup(@RequestBody SignupRequest request) {
        // 210 validation: duplicate email
        Optional<User> existing = userService.findByEmail(request.getEmail());
        if (existing.isPresent()) {
            return ResponseEntity.status(210).body("Email id already exists");
        }
        AuthResponse response = authService.signup(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody LoginRequest request) {
        // 210 validation: wrong password when email exists
        Optional<User> userOpt = userService.findByEmail(request.getEmail());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (!user.getPassword().equals(request.getPassword())) {
                return ResponseEntity.status(210).body("Invalid password");
            }
        }
        Optional<AuthResponse> response = authService.login(request);
        if (response.isPresent()) {
            return ResponseEntity.ok(response.get());
        }
        return ResponseEntity.status(401).body("Invalid credentials");
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            authService.logout(authHeader.substring(7));
        }
        return ResponseEntity.ok("Logged out successfully");
    }
}
