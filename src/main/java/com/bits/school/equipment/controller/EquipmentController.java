package com.bits.school.equipment.controller;

import com.bits.school.equipment.entity.Equipment;
import com.bits.school.equipment.entity.User;
import com.bits.school.equipment.service.AuthService;
import com.bits.school.equipment.service.EquipmentService;
import com.bits.school.equipment.util.AuthUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/equipment")
@CrossOrigin(origins = "http://localhost:5173/", allowCredentials = "true")

public class EquipmentController {
    private final EquipmentService equipmentService;
    private final AuthService authService;

    public EquipmentController(EquipmentService equipmentService, AuthService authService) {
        this.equipmentService = equipmentService;
        this.authService = authService;
    }
    private static final String AUTHENTICATION_REQUIRED = "Authentication required";

    @GetMapping
    public ResponseEntity<Object> listAll(
            @RequestParam(required = false) String category,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User user = validateToken(authHeader);
        if (user == null) {
            return ResponseEntity.status(401).body("Authentication required");
        }

        List<Equipment> items = (category != null)
                ? equipmentService.findByCategory(category)
                : equipmentService.findAll();
        // Hide equipment that is currently under maintenance
        List<Equipment> visible = items.stream()
                .filter(e -> !e.isUnderMaintenance())
                .collect(Collectors.toList());
        return ResponseEntity.ok(visible);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> get(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User user = validateToken(authHeader);
        if (user == null) {
            return ResponseEntity.status(401).body(AUTHENTICATION_REQUIRED);
        }

        Optional<Equipment> e = equipmentService.findById(id);
        // map to ResponseEntity<Object> to avoid incompatible generic types
        return e.map(eq -> ResponseEntity.ok((Object) eq))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Object> create(
            @RequestBody Equipment equipment,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User user = validateToken(authHeader);
        if (user == null) {
            return ResponseEntity.status(401).body(AUTHENTICATION_REQUIRED);
        }

        // Only ADMIN can create equipment
        AuthUtil.requireAdmin(user);

        Equipment saved = equipmentService.createOrUpdate(equipment);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> update(
            @PathVariable Long id,
            @RequestBody Equipment equipment,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User user = validateToken(authHeader);
        if (user == null) {
            return ResponseEntity.status(401).body(AUTHENTICATION_REQUIRED);
        }

        // Only ADMIN can update equipment
        AuthUtil.requireAdmin(user);

        Optional<Equipment> existing = equipmentService.findById(id);
        if (existing.isEmpty()) return ResponseEntity.notFound().build();
        equipment.setId(id);
        return ResponseEntity.ok(equipmentService.createOrUpdate(equipment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User user = validateToken(authHeader);
        if (user == null) {
            return ResponseEntity.status(401).body(AUTHENTICATION_REQUIRED);
        }

        // Only ADMIN can delete equipment
        AuthUtil.requireAdmin(user);

        equipmentService.deleteById(id);
        return ResponseEntity.ok("Equipment deleted successfully");
    }

    private User validateToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        return authService.validateToken(token).orElse(null);
    }
}
