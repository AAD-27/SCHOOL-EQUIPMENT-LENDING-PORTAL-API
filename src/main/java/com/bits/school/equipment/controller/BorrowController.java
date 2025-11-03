package com.bits.school.equipment.controller;

import com.bits.school.equipment.entity.BorrowRequest;
import com.bits.school.equipment.entity.Equipment;
import com.bits.school.equipment.entity.User;
import com.bits.school.equipment.service.AuthService;
import com.bits.school.equipment.service.BorrowRequestService;
import com.bits.school.equipment.service.EquipmentService;
import com.bits.school.equipment.service.UserService;
import com.bits.school.equipment.util.AuthUtil;
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
    private final AuthService authService;

    public BorrowController(BorrowRequestService borrowRequestService, EquipmentService equipmentService,
                           UserService userService, AuthService authService) {
        this.borrowRequestService = borrowRequestService;
        this.equipmentService = equipmentService;
        this.userService = userService;
        this.authService = authService;
    }

    @PostMapping("/request")
    public ResponseEntity<?> requestBorrow(
            @RequestBody BorrowRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User user = validateToken(authHeader);
        if (user == null) {
            return ResponseEntity.status(401).body("Authentication required");
        }

        // Basic quantity validation
        Integer qty = request.getQuantityRequested();
        if (qty == null || qty <= 0) {
            return ResponseEntity.status(210).body("Please enter a valid quantity");
        }

        // Validate equipment exists (handle nulls safely)
        Long equipmentId = request.getEquipment() != null ? request.getEquipment().getId() : null;
        String requestedName = request.getEquipment() != null ? request.getEquipment().getName() : null;
        Optional<Equipment> e = (equipmentId != null) ? equipmentService.findById(equipmentId) : Optional.empty();
        if (e.isEmpty()) {
            String msg = (requestedName != null && !requestedName.isBlank())
                    ? requestedName + " is invalid"
                    : "Equipment is invalid";
            return ResponseEntity.status(210).body(msg);
        }

        Equipment equipment = e.get();
        if (equipment.getAvailableQuantity() == null || equipment.getAvailableQuantity() < qty) {
            String nm = (equipment.getName() != null && !equipment.getName().isBlank()) ? equipment.getName() : "Equipment";
            return ResponseEntity.status(210).body(nm + " is not available");
        }

        // Set the requester as the authenticated user
        request.setRequester(user);
        request.setEquipment(equipment);
        request.setStatus(BorrowRequest.Status.PENDING);

        BorrowRequest saved = borrowRequestService.createRequest(request);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<?> listAll(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User user = validateToken(authHeader);
        if (user == null) {
            return ResponseEntity.status(401).body("Authentication required");
        }

        // Students see only their requests, Staff/Admin see all
        if (AuthUtil.isStudent(user)) {
            return ResponseEntity.ok(borrowRequestService.findByRequesterId(user.getId()));
        }
        return ResponseEntity.ok(borrowRequestService.findAll());
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approve(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User user = validateToken(authHeader);
        if (user == null) {
            return ResponseEntity.status(401).body("Authentication required");
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
    public ResponseEntity<?> markReturned(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User user = validateToken(authHeader);
        if (user == null) {
            return ResponseEntity.status(401).body("Authentication required");
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
    public ResponseEntity<?> reject(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = validateToken(authHeader);
        if (user == null) {
            return ResponseEntity.status(401).body("Authentication required");
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
