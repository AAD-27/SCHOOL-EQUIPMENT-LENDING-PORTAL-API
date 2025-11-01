package com.bits.school.equipment.dao.impl;

import com.bits.school.equipment.dao.EquipmentDao;
import com.bits.school.equipment.entity.Equipment;
import com.bits.school.equipment.repository.EquipmentRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class EquipmentDaoImpl implements EquipmentDao {
    private final EquipmentRepository equipmentRepository;

    public EquipmentDaoImpl(EquipmentRepository equipmentRepository) {
        this.equipmentRepository = equipmentRepository;
    }

    @Override
    public Equipment save(Equipment equipment) {
        return equipmentRepository.save(equipment);
    }

    @Override
    public Optional<Equipment> findById(Long id) {
        return equipmentRepository.findById(id);
    }

    @Override
    public List<Equipment> findAll() {
        return equipmentRepository.findAll();
    }

    @Override
    public List<Equipment> findByCategory(String category) {
        return equipmentRepository.findByCategory(category);
    }

    @Override
    public void deleteById(Long id) {
        equipmentRepository.deleteById(id);
    }
}

