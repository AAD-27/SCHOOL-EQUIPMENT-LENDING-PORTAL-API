package com.bits.school.equipment.service;

import com.bits.school.equipment.dto.DamageReportDto;
import com.bits.school.equipment.entity.DamageReport;

import java.util.List;
import java.util.Optional;

public interface DamageReportService {
    DamageReport create(DamageReportDto dto);
    Optional<DamageReport> findById(Long id);
    List<DamageReport> findAll();
    // equipmentId refers to the original equipment id that was reported damaged
    List<DamageReport> findByEquipmentId(Long equipmentId);
    DamageReport markInMaintenance(Long id);
    DamageReport markRepaired(Long id);
}
