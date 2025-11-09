package com.bits.school.equipment.controller;

import com.bits.school.equipment.dto.DamageReportDto;
import com.bits.school.equipment.entity.DamageReport;
import com.bits.school.equipment.entity.User;
import com.bits.school.equipment.service.AuthService;
import com.bits.school.equipment.service.DamageReportService;
import com.bits.school.equipment.util.AuthUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/damages")
@CrossOrigin(origins = "http://localhost:5173/", allowCredentials = "true")
public class DamageController {

    private final DamageReportService damageReportService;
    private final AuthService authService;

    public DamageController(DamageReportService damageReportService, AuthService authService) {
        this.damageReportService = damageReportService;
        this.authService = authService;
    }

    private User validateToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        return authService.validateToken(token).orElse(null);
    }

    private DamageReportDto toDto(DamageReport dr) {
        DamageReportDto dto = new DamageReportDto();
        dto.setId(dr.getId());
        dto.setEquipmentId(dr.getOriginalEquipmentId());
        dto.setDescription(dr.getDescription());
        dto.setStatus(dr.getStatus().name());
        dto.setCreatedAt(dr.getCreatedAt());
        dto.setUpdatedAt(dr.getUpdatedAt());
        dto.setRepairedEquipmentId(dr.getRepairedEquipmentId());
        return dto;
    }

    @PostMapping
    public ResponseEntity<Object> create(@RequestBody DamageReportDto dto,
                                         @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = validateToken(authHeader);
        AuthUtil.requireAdmin(user);
        if (dto.getEquipmentId() == null || dto.getDescription() == null || dto.getDescription().isBlank()) {
            return ResponseEntity.status(210).body("equipmentId and description are required");
        }
        try {
            DamageReport dr = damageReportService.create(dto);
            return ResponseEntity.ok(toDto(dr));
        } catch (IllegalStateException e) { // active borrow requests
            return ResponseEntity.status(210).body(e.getMessage());
        } catch (IllegalArgumentException e) { // invalid equipment id
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (DataIntegrityViolationException e) { // likely leftover FK/column in DB
            return ResponseEntity.status(500).body("Schema mismatch: please run DB migration to drop damage_reports.equipment_id");
        }
    }

    @GetMapping
    public ResponseEntity<Object> listAll(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = validateToken(authHeader);
        AuthUtil.requireAdmin(user);
        List<DamageReportDto> list = damageReportService.findAll().stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> get(@PathVariable Long id,
                                      @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = validateToken(authHeader);
        AuthUtil.requireAdmin(user);
        return damageReportService.findById(id)
                .map(dr -> ResponseEntity.ok((Object) toDto(dr)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Object> approve(@PathVariable Long id,
                                          @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = validateToken(authHeader);
        AuthUtil.requireAdmin(user);
        try {
            DamageReport dr = damageReportService.markInMaintenance(id);
            return ResponseEntity.ok(toDto(dr));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/repaired")
    public ResponseEntity<Object> repaired(@PathVariable Long id,
                                           @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = validateToken(authHeader);
        AuthUtil.requireAdmin(user);
        try {
            DamageReport dr = damageReportService.markRepaired(id);
            return ResponseEntity.ok(toDto(dr));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }
}
