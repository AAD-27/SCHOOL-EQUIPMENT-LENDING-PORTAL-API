package com.bits.school.equipment.repository;

import com.bits.school.equipment.entity.DamageReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DamageReportRepository extends JpaRepository<DamageReport, Long> {
    // Find damage reports referencing original equipment id (snapshot id)
    List<DamageReport> findByOriginalEquipmentId(Long originalEquipmentId);
}
