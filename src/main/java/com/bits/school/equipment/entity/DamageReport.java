package com.bits.school.equipment.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "damage_reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DamageReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Snapshot of original equipment (so we can recreate later)
    @Column(name = "original_equipment_id")
    private Long originalEquipmentId;
    private String equipmentName;
    private String equipmentCategory;
    private String equipmentConditionDescription;
    private Integer equipmentQuantity;
    private Integer equipmentAvailableQuantity;

    // New equipment id after repair (null until recreated)
    private Long repairedEquipmentId;

    public enum Status {
        REPORTED,
        IN_MAINTENANCE,
        EQUIPMENT_REPAIRED
    }
}
