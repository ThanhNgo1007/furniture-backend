package com.furniture.controller;

import com.furniture.domain.AccountStatus;
import com.furniture.domain.USER_ROLE;
import com.furniture.modal.Seller;
import com.furniture.modal.User;
import com.furniture.service.SellerService;
import com.furniture.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
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

    // 1. API lấy danh sách tất cả User
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers(@RequestHeader("Authorization") String jwt) throws Exception {
        List<User> users = userService.findAllUsers();
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
