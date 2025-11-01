package com.bits.school.equipment.service.impl;

import com.bits.school.equipment.dao.EquipmentDao;
import com.bits.school.equipment.entity.Equipment;
import com.bits.school.equipment.service.EquipmentService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EquipmentServiceImpl implements EquipmentService {
    private final EquipmentDao equipmentDao;

    public EquipmentServiceImpl(EquipmentDao equipmentDao) {
        this.equipmentDao = equipmentDao;
    }

    @Override
    public Equipment createOrUpdate(Equipment equipment) {
        return equipmentDao.save(equipment);
    }

    @Override
    public Optional<Equipment> findById(Long id) {
        return equipmentDao.findById(id);
    }

    @Override
    public List<Equipment> findAll() {
        return equipmentDao.findAll();
    }

    @Override
    public List<Equipment> findByCategory(String category) {
        return equipmentDao.findByCategory(category);
    }

    @Override
    public void deleteById(Long id) {
        equipmentDao.deleteById(id);
    }
}

