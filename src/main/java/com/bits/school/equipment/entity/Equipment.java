package com.bits.school.equipment.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "equipment")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Equipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String category;
    private String conditionDescription;
    private Integer quantity;
    private Integer availableQuantity;
    // New: if true, this item is hidden from general listing while in maintenance
    @Column(nullable = false)
    private boolean underMaintenance = false;
}
