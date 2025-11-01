package com.bits.school.equipment.dao;

import com.bits.school.equipment.entity.Equipment;

import java.util.List;
import java.util.Optional;

public interface EquipmentDao {
    Equipment save(Equipment equipment);
    Optional<Equipment> findById(Long id);
    List<Equipment> findAll();
    List<Equipment> findByCategory(String category);
    void deleteById(Long id);
}

