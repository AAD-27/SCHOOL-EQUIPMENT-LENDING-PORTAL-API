package com.bits.school.equipment.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DamageReportDto {
    private Long id;
    // For requests: original equipment id to report damage
    private Long equipmentId;
    private String description;
    private String status; // REPORTED | IN_MAINTENANCE | EQUIPMENT_REPAIRED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // For responses: track repaired equipment id (if recreated)
    private Long repairedEquipmentId;
}
