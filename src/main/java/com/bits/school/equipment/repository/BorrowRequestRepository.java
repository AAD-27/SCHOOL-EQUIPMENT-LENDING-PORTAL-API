package com.bits.school.equipment.repository;

import com.bits.school.equipment.entity.BorrowRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BorrowRequestRepository extends JpaRepository<BorrowRequest, Long> {
    List<BorrowRequest> findByRequesterId(Long requesterId);
    List<BorrowRequest> findByEquipmentId(Long equipmentId);
}

