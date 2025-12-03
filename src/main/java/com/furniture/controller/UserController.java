package com.furniture.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
}
