package com.furniture.controller;

import com.furniture.modal.Address;
import com.furniture.modal.User;
import com.furniture.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
