package com.bits.school.equipment.controller;

import com.bits.school.equipment.dto.BorrowRequestDto;
import com.bits.school.equipment.entity.BorrowRequest;
import com.bits.school.equipment.entity.Equipment;
import com.bits.school.equipment.entity.User;
import com.bits.school.equipment.service.AuthService;
import com.bits.school.equipment.service.BorrowRequestService;
import com.bits.school.equipment.service.EquipmentService;
import com.bits.school.equipment.util.AuthUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/borrow")
@CrossOrigin(origins = "http://localhost:5173/", allowCredentials = "true")

public class BorrowController {
    private final BorrowRequestService borrowRequestService;
    private final EquipmentService equipmentService;
    private final AuthService authService;

    public BorrowController(BorrowRequestService borrowRequestService, EquipmentService equipmentService,
                           AuthService authService) {
        this.borrowRequestService = borrowRequestService;
        this.equipmentService = equipmentService;
        this.authService = authService;
    }
    private static final String AUTHENTICATION_REQUIRED = "Authentication required";

    @PostMapping("/request")
    public ResponseEntity<Object> requestBorrow(
            @RequestBody BorrowRequestDto dto,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User user = validateToken(authHeader);
        if (user == null) {
            return ResponseEntity.status(401).body(AUTHENTICATION_REQUIRED);
        }

        // Basic quantity validation
        Integer qty = dto.getQuantityRequested();
        if (qty == null || qty <= 0) {
            return ResponseEntity.status(210).body("Please enter a valid quantity");
        }

        // Validate equipment exists (handle nulls safely)
        Long equipmentId = dto.getEquipmentId();
        Optional<Equipment> e = (equipmentId != null) ? equipmentService.findById(equipmentId) : Optional.empty();
        if (e.isEmpty()) {
            return ResponseEntity.status(210).body("Equipment is invalid");
        }

        Equipment equipment = e.get();
        if (equipment.getAvailableQuantity() == null || equipment.getAvailableQuantity() < qty) {
            String nm = (equipment.getName() != null && !equipment.getName().isBlank()) ? equipment.getName() : "Equipment";
            return ResponseEntity.status(210).body(nm + " is not available");
        }

        // Map DTO to entity and set the authenticated user as requester
        BorrowRequest request = new BorrowRequest();
        request.setQuantityRequested(dto.getQuantityRequested());
        request.setFromDate(dto.getFromDate());
        request.setToDate(dto.getToDate());
        request.setRequester(user);
        request.setEquipment(equipment);
        request.setStatus(BorrowRequest.Status.PENDING);

        BorrowRequest saved = borrowRequestService.createRequest(request);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<Object> listAll(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User user = validateToken(authHeader);
        if (user == null) {
            return ResponseEntity.status(401).body(AUTHENTICATION_REQUIRED);
        }

        // Students see only their requests, Staff/Admin see all
        if (AuthUtil.isStudent(user)) {
            return ResponseEntity.ok(borrowRequestService.findByRequesterId(user.getId()));
        }
        return ResponseEntity.ok(borrowRequestService.findAll());
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Object> approve(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User user = validateToken(authHeader);
        if (user == null) {
            return ResponseEntity.status(401).body(AUTHENTICATION_REQUIRED);
        }

        // Only STAFF or ADMIN can approve
        AuthUtil.requireStaffOrAdmin(user);

        Optional<BorrowRequest> r = borrowRequestService.findById(id);
        if (r.isEmpty()) return ResponseEntity.notFound().build();

        BorrowRequest req = r.get();
        if (req.getStatus() != BorrowRequest.Status.PENDING) {
            return ResponseEntity.status(210).body("Only pending requests can be approved");
        }
        Equipment eq = req.getEquipment();
        if (eq.getAvailableQuantity() < req.getQuantityRequested()) {
            return ResponseEntity.status(210).body("Insufficient quantity to approve");
        }

        eq.setAvailableQuantity(eq.getAvailableQuantity() - req.getQuantityRequested());
        equipmentService.createOrUpdate(eq);
        req.setStatus(BorrowRequest.Status.APPROVED);
        borrowRequestService.createRequest(req);
        return ResponseEntity.ok(req);
    }

    @PostMapping("/{id}/return")
    public ResponseEntity<Object> markReturned(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User user = validateToken(authHeader);
        if (user == null) {
            return ResponseEntity.status(401).body(AUTHENTICATION_REQUIRED);
        }

        // Only STAFF or ADMIN can mark as returned
        AuthUtil.requireStaffOrAdmin(user);

        Optional<BorrowRequest> r = borrowRequestService.findById(id);
        if (r.isEmpty()) return ResponseEntity.notFound().build();

        BorrowRequest req = r.get();
        if (req.getStatus() != BorrowRequest.Status.APPROVED) {
            return ResponseEntity.status(210).body("Only approved requests can be marked as returned");
        }
        Equipment eq = req.getEquipment();
        eq.setAvailableQuantity(eq.getAvailableQuantity() + req.getQuantityRequested());
        equipmentService.createOrUpdate(eq);
        req.setStatus(BorrowRequest.Status.RETURNED);
        borrowRequestService.createRequest(req);
        return ResponseEntity.ok(req);
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<Object> reject(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = validateToken(authHeader);
        if (user == null) {
            return ResponseEntity.status(401).body(AUTHENTICATION_REQUIRED);
        }

        // Only STAFF or ADMIN can reject
        AuthUtil.requireStaffOrAdmin(user);

        Optional<BorrowRequest> r = borrowRequestService.findById(id);
        if (r.isEmpty()) return ResponseEntity.notFound().build();

        BorrowRequest req = r.get();
        if (req.getStatus() != BorrowRequest.Status.PENDING) {
            return ResponseEntity.status(210).body("Only pending requests can be rejected");
        }
        // No inventory change on reject
        req.setStatus(BorrowRequest.Status.REJECTED);
        borrowRequestService.createRequest(req);
        return ResponseEntity.ok(req);
    }

    private User validateToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        return authService.validateToken(token).orElse(null);
    }
}
