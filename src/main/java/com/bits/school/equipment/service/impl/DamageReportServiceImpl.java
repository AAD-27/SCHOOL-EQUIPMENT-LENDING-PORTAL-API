package com.bits.school.equipment.service.impl;

import com.bits.school.equipment.dto.DamageReportDto;
import com.bits.school.equipment.entity.BorrowRequest;
import com.bits.school.equipment.entity.DamageReport;
import com.bits.school.equipment.entity.Equipment;
import com.bits.school.equipment.repository.BorrowRequestRepository;
import com.bits.school.equipment.repository.DamageReportRepository;
import com.bits.school.equipment.service.DamageReportService;
import com.bits.school.equipment.service.EquipmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DamageReportServiceImpl implements DamageReportService {

    private final DamageReportRepository damageReportRepository;
    private final EquipmentService equipmentService;
    private final BorrowRequestRepository borrowRequestRepository;

    public DamageReportServiceImpl(DamageReportRepository damageReportRepository,
                                   EquipmentService equipmentService,
                                   BorrowRequestRepository borrowRequestRepository) {
        this.damageReportRepository = damageReportRepository;
        this.equipmentService = equipmentService;
        this.borrowRequestRepository = borrowRequestRepository;
    }

    @Override
    public DamageReport create(DamageReportDto dto) {
        if (dto.getEquipmentId() == null) {
            throw new IllegalArgumentException("equipmentId is required");
        }
        Equipment equipment = equipmentService.findById(dto.getEquipmentId())
                .orElseThrow(() -> new IllegalArgumentException("Equipment not found"));

        // Guard: block deletion when there are active borrow requests for this equipment
        List<BorrowRequest> related = borrowRequestRepository.findByEquipmentId(equipment.getId());
        boolean hasActive = related.stream().anyMatch(br -> br.getStatus() == BorrowRequest.Status.PENDING
                || br.getStatus() == BorrowRequest.Status.APPROVED
                || br.getStatus() == BorrowRequest.Status.ISSUED);
        if (hasActive) {
            throw new IllegalStateException("Cannot create damage report: equipment has active borrow requests");
        }

        DamageReport dr = new DamageReport();
        dr.setDescription(dto.getDescription());
        dr.setStatus(DamageReport.Status.REPORTED);
        dr.setCreatedAt(LocalDateTime.now());
        dr.setUpdatedAt(LocalDateTime.now());
        // Snapshot original equipment fields
        dr.setOriginalEquipmentId(equipment.getId());
        dr.setEquipmentName(equipment.getName());
        dr.setEquipmentCategory(equipment.getCategory());
        dr.setEquipmentConditionDescription(equipment.getConditionDescription());
        dr.setEquipmentQuantity(equipment.getQuantity());
        dr.setEquipmentAvailableQuantity(equipment.getAvailableQuantity());
        // Save report and delete equipment to hide it from listings
        DamageReport saved = damageReportRepository.save(dr);
        equipmentService.deleteById(equipment.getId());
        return saved;
    }

    @Override
    public Optional<DamageReport> findById(Long id) {
        return damageReportRepository.findById(id);
    }

    @Override
    public List<DamageReport> findAll() {
        return damageReportRepository.findAll();
    }

    @Override
    public List<DamageReport> findByEquipmentId(Long equipmentId) {
        return damageReportRepository.findByOriginalEquipmentId(equipmentId);
    }

    @Override
    public DamageReport markInMaintenance(Long id) {
        DamageReport dr = damageReportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Damage report not found"));
        dr.setStatus(DamageReport.Status.IN_MAINTENANCE);
        dr.setUpdatedAt(LocalDateTime.now());
        return damageReportRepository.save(dr);
    }

    @Override
    public DamageReport markRepaired(Long id) {
        DamageReport dr = damageReportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Damage report not found"));
        // Recreate equipment from snapshot
        Equipment eq = new Equipment();
        eq.setName(dr.getEquipmentName());
        eq.setCategory(dr.getEquipmentCategory());
        eq.setConditionDescription(dr.getEquipmentConditionDescription());
        eq.setQuantity(dr.getEquipmentQuantity());
        eq.setAvailableQuantity(dr.getEquipmentAvailableQuantity());
        eq.setUnderMaintenance(false);
        Equipment recreated = equipmentService.createOrUpdate(eq);
        dr.setRepairedEquipmentId(recreated.getId());
        dr.setStatus(DamageReport.Status.EQUIPMENT_REPAIRED);
        dr.setUpdatedAt(LocalDateTime.now());
        return damageReportRepository.save(dr);
    }
}
