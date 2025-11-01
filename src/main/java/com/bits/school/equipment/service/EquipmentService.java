package com.bits.school.equipment.service;

import com.bits.school.equipment.entity.Equipment;

import java.util.List;
import java.util.Optional;

public interface EquipmentService {
    Equipment createOrUpdate(Equipment equipment);
    Optional<Equipment> findById(Long id);
    List<Equipment> findAll();
    List<Equipment> findByCategory(String category);
    void deleteById(Long id);
}

