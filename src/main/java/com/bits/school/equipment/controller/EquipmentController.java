package com.bits.school.equipment.controller;

import com.bits.school.equipment.entity.Equipment;
import com.bits.school.equipment.service.EquipmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/equipment")
public class EquipmentController {
    private final EquipmentService equipmentService;

    public EquipmentController(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    @GetMapping
    public ResponseEntity<List<Equipment>> listAll(@RequestParam(required = false) String category) {
        if (category != null) {
            return ResponseEntity.ok(equipmentService.findByCategory(category));
        }
        return ResponseEntity.ok(equipmentService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Equipment> get(@PathVariable Long id) {
        Optional<Equipment> e = equipmentService.findById(id);
        return e.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Equipment> create(@RequestBody Equipment equipment) {
        Equipment saved = equipmentService.createOrUpdate(equipment);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Equipment> update(@PathVariable Long id, @RequestBody Equipment equipment) {
        Optional<Equipment> existing = equipmentService.findById(id);
        if (existing.isEmpty()) return ResponseEntity.notFound().build();
        equipment.setId(id);
        return ResponseEntity.ok(equipmentService.createOrUpdate(equipment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        equipmentService.deleteById(id);
        return ResponseEntity.ok().build();
    }
}

