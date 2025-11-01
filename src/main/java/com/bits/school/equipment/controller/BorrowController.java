package com.bits.school.equipment.controller;

import com.bits.school.equipment.entity.BorrowRequest;
import com.bits.school.equipment.entity.Equipment;
import com.bits.school.equipment.entity.User;
import com.bits.school.equipment.service.BorrowRequestService;
import com.bits.school.equipment.service.EquipmentService;
import com.bits.school.equipment.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/borrow")
public class BorrowController {
    private final BorrowRequestService borrowRequestService;
    private final EquipmentService equipmentService;
    private final UserService userService;

    public BorrowController(BorrowRequestService borrowRequestService, EquipmentService equipmentService, UserService userService) {
        this.borrowRequestService = borrowRequestService;
        this.equipmentService = equipmentService;
        this.userService = userService;
    }

    @PostMapping("/request")
    public ResponseEntity<?> requestBorrow(@RequestBody BorrowRequest request) {
        // basic validation: equipment and user exist and quantity available
        Optional<User> u = userService.findById(request.getRequester().getId());
        Optional<Equipment> e = equipmentService.findById(request.getEquipment().getId());
        if (u.isEmpty() || e.isEmpty()) return ResponseEntity.badRequest().body("Invalid user or equipment");
        Equipment equipment = e.get();
        if (equipment.getAvailableQuantity() == null || equipment.getAvailableQuantity() < request.getQuantityRequested()) {
            return ResponseEntity.badRequest().body("Not enough items available");
        }
        request.setRequester(u.get());
        request.setEquipment(equipment);
        request.setStatus(BorrowRequest.Status.PENDING);
        BorrowRequest saved = borrowRequestService.createRequest(request);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<List<BorrowRequest>> listAll() {
        return ResponseEntity.ok(borrowRequestService.findAll());
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable Long id) {
        Optional<BorrowRequest> r = borrowRequestService.findById(id);
        if (r.isEmpty()) return ResponseEntity.notFound().build();
        BorrowRequest req = r.get();
        Equipment eq = req.getEquipment();
        if (eq.getAvailableQuantity() < req.getQuantityRequested()) return ResponseEntity.badRequest().body("Insufficient quantity to approve");
        eq.setAvailableQuantity(eq.getAvailableQuantity() - req.getQuantityRequested());
        equipmentService.createOrUpdate(eq);
        req.setStatus(BorrowRequest.Status.APPROVED);
        borrowRequestService.createRequest(req);
        return ResponseEntity.ok(req);
    }

    @PostMapping("/{id}/return")
    public ResponseEntity<?> markReturned(@PathVariable Long id) {
        Optional<BorrowRequest> r = borrowRequestService.findById(id);
        if (r.isEmpty()) return ResponseEntity.notFound().build();
        BorrowRequest req = r.get();
        Equipment eq = req.getEquipment();
        eq.setAvailableQuantity(eq.getAvailableQuantity() + req.getQuantityRequested());
        equipmentService.createOrUpdate(eq);
        req.setStatus(BorrowRequest.Status.RETURNED);
        borrowRequestService.createRequest(req);
        return ResponseEntity.ok(req);
    }
}

