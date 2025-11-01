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

        // Validate equipment exists and has availability
        Optional<Equipment> e = equipmentService.findById(request.getEquipment().getId());
        if (e.isEmpty()) return ResponseEntity.badRequest().body("Invalid equipment");

        Equipment equipment = e.get();
        if (equipment.getAvailableQuantity() == null || equipment.getAvailableQuantity() < request.getQuantityRequested()) {
            return ResponseEntity.badRequest().body("Not enough items available");
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
        Equipment eq = req.getEquipment();
        if (eq.getAvailableQuantity() < req.getQuantityRequested()) {
            return ResponseEntity.badRequest().body("Insufficient quantity to approve");
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
        Equipment eq = req.getEquipment();
        eq.setAvailableQuantity(eq.getAvailableQuantity() + req.getQuantityRequested());
        equipmentService.createOrUpdate(eq);
        req.setStatus(BorrowRequest.Status.RETURNED);
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
