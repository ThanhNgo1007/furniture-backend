package com.furniture.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.furniture.modal.Address;
import com.furniture.modal.User;
import com.furniture.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<User> UserProfileHandler(
            @RequestHeader("Authorization") String jwt
            ) throws Exception {

        User user = userService.findUserByJwtToken(jwt);


        return ResponseEntity.ok(user);
    }

    @PostMapping("/address/add")
    public ResponseEntity<User> createAddressHandler(
            @RequestHeader("Authorization") String jwt,
            @RequestBody Address address
    ) throws Exception {
        User user = userService.findUserByJwtToken(jwt);
        User updatedUser = userService.createAddress(user, address);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/address/{addressId}")
    public ResponseEntity<User> updateAddressHandler(
            @RequestHeader("Authorization") String jwt,
            @PathVariable Long addressId,
            @RequestBody Address address
    ) throws Exception {
        User user = userService.findUserByJwtToken(jwt);
        User updatedUser = userService.updateAddress(user.getId(), addressId, address);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/address/{addressId}")
    public ResponseEntity<User> deleteAddressHandler(
            @RequestHeader("Authorization") String jwt,
            @PathVariable Long addressId
    ) throws Exception {
        User user = userService.findUserByJwtToken(jwt);
        User updatedUser = userService.deleteAddress(user.getId(), addressId);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/address/{addressId}/default")
    public ResponseEntity<User> setDefaultAddressHandler(
            @RequestHeader("Authorization") String jwt,
            @PathVariable Long addressId
    ) throws Exception {
        User user = userService.findUserByJwtToken(jwt);
        User updatedUser = userService.setDefaultAddress(user.getId(), addressId);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/profile")
    public ResponseEntity<User> updateProfileHandler(
            @RequestHeader("Authorization") String jwt,
            @RequestBody UpdateProfileRequest request
    ) throws Exception {
        User updatedUser = userService.updateUserProfile(jwt, request.getFullName(), request.getMobile());
        return ResponseEntity.ok(updatedUser);
    }

    // DTO for update request
    public static class UpdateProfileRequest {
        private String fullName;
        private String mobile;

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getMobile() { return mobile; }
        public void setMobile(String mobile) { this.mobile = mobile; }
    }
}
