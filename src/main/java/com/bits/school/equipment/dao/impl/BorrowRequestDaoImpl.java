package com.bits.school.equipment.dao.impl;

import com.bits.school.equipment.dao.BorrowRequestDao;
import com.bits.school.equipment.entity.BorrowRequest;
import com.bits.school.equipment.repository.BorrowRequestRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class BorrowRequestDaoImpl implements BorrowRequestDao {
    private final BorrowRequestRepository borrowRequestRepository;

    public BorrowRequestDaoImpl(BorrowRequestRepository borrowRequestRepository) {
        this.borrowRequestRepository = borrowRequestRepository;
    }

    @Override
    public BorrowRequest save(BorrowRequest request) {
        return borrowRequestRepository.save(request);
    }

    @Override
    public Optional<BorrowRequest> findById(Long id) {
        return borrowRequestRepository.findById(id);
    }

    @Override
    public List<BorrowRequest> findAll() {
        return borrowRequestRepository.findAll();
    }

    @Override
    public List<BorrowRequest> findByRequesterId(Long requesterId) {
        return borrowRequestRepository.findByRequesterId(requesterId);
    }

    @Override
    public List<BorrowRequest> findByEquipmentId(Long equipmentId) {
        return borrowRequestRepository.findByEquipmentId(equipmentId);
    }
}

