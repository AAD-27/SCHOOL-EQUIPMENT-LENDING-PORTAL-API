package com.bits.school.equipment.service.impl;

import com.bits.school.equipment.dao.BorrowRequestDao;
import com.bits.school.equipment.entity.BorrowRequest;
import com.bits.school.equipment.service.BorrowRequestService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BorrowRequestServiceImpl implements BorrowRequestService {
    private final BorrowRequestDao borrowRequestDao;

    public BorrowRequestServiceImpl(BorrowRequestDao borrowRequestDao) {
        this.borrowRequestDao = borrowRequestDao;
    }

    @Override
    public BorrowRequest createRequest(BorrowRequest request) {
        return borrowRequestDao.save(request);
    }

    @Override
    public Optional<BorrowRequest> findById(Long id) {
        return borrowRequestDao.findById(id);
    }

    @Override
    public List<BorrowRequest> findAll() {
        return borrowRequestDao.findAll();
    }

    @Override
    public List<BorrowRequest> findByRequesterId(Long requesterId) {
        return borrowRequestDao.findByRequesterId(requesterId);
    }

    @Override
    public List<BorrowRequest> findByEquipmentId(Long equipmentId) {
        return borrowRequestDao.findByEquipmentId(equipmentId);
    }
}

