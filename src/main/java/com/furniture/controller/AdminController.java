package com.furniture.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.furniture.domain.AccountStatus;
import com.furniture.domain.USER_ROLE;
import com.furniture.modal.Seller;
import com.furniture.modal.User;
import com.furniture.service.SellerService;
import com.furniture.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    private final SellerService sellerService;
    private final UserService userService;

    @PatchMapping("/seller/{id}/status/{status}")
    public ResponseEntity<Seller> updateSellerStatus(
            @PathVariable Long id,
            @PathVariable AccountStatus status
    ) throws Exception {

        Seller updatedSeller = sellerService.updateSellerAccountStatus(id, status);

        return ResponseEntity.ok(updatedSeller);
    }

    // Admin Seller Management Endpoints
    @GetMapping("/sellers")
    public ResponseEntity<List<Seller>> getAllSellers(
            @RequestParam(required = false) AccountStatus status
    ) {
        List<Seller> sellers = sellerService.getAllSellers(status);
        return ResponseEntity.ok(sellers);
    }

    @GetMapping("/sellers/paginated")
    public ResponseEntity<org.springframework.data.domain.Page<Seller>> getAllSellersPaginated(
            @RequestParam(required = false) AccountStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        org.springframework.data.domain.Pageable pageable = 
            org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<Seller> sellers = 
            sellerService.getAllSellersPaginated(status, pageable);
        return ResponseEntity.ok(sellers);
    }

    @PatchMapping("/sellers/{id}/status")
    public ResponseEntity<Seller> updateSellerAccountStatus(
            @PathVariable Long id,
            @RequestParam AccountStatus status
    ) throws Exception {
        Seller updatedSeller = sellerService.updateSellerAccountStatus(id, status);
        return ResponseEntity.ok(updatedSeller);
    }

    // 1. API lấy danh sách tất cả User (legacy - no pagination)
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers(@RequestHeader("Authorization") String jwt) throws Exception {
        List<User> users = userService.findAllUsers();
        return ResponseEntity.ok(users);
    }

    // 1b. API lấy danh sách User với pagination
    @GetMapping("/users/paginated")
    public ResponseEntity<org.springframework.data.domain.Page<User>> getAllUsersPaginated(
            @RequestHeader("Authorization") String jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) throws Exception {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = userService.findAllUsersPaginated(pageable);
        return ResponseEntity.ok(users);
    }

    // 2. API đổi quyền User
    @PutMapping("/users/{userId}/role")
    public ResponseEntity<User> updateUserRole(
            @RequestHeader("Authorization") String jwt,
            @PathVariable Long userId,
            @RequestParam USER_ROLE role // Truyền role qua param (VD: ?role=ROLE_ADMIN)
    ) throws Exception {
        User updatedUser = userService.updateRole(userId, role);
        return ResponseEntity.ok(updatedUser);
    }
}
