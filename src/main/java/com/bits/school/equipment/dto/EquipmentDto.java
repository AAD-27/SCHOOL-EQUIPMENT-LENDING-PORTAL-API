package com.bits.school.equipment.dto;

import lombok.Data;

@Data
public class EquipmentDto {
    private Long id;
    private String name;
    private String category;
    private String conditionDescription;
    private Integer quantity;
    private Integer availableQuantity;
}

