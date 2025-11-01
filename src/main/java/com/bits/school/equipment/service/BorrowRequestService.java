package com.bits.school.equipment.service;

import com.bits.school.equipment.entity.BorrowRequest;

import java.util.List;
import java.util.Optional;

public interface BorrowRequestService {
    BorrowRequest createRequest(BorrowRequest request);
    Optional<BorrowRequest> findById(Long id);
    List<BorrowRequest> findAll();
    List<BorrowRequest> findByRequesterId(Long requesterId);
    List<BorrowRequest> findByEquipmentId(Long equipmentId);
}

