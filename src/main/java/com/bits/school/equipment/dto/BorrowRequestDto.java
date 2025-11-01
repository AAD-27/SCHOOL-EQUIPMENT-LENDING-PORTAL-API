package com.bits.school.equipment.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BorrowRequestDto {
    private Long id;
    private Long requesterId;
    private Long equipmentId;
    private Integer quantityRequested;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
    private String status;
}

