package com.furniture.controller;

import com.furniture.domain.USER_ROLE;
import com.furniture.modal.User;
import com.furniture.response.AuthResponse;
import com.furniture.response.SignupRequest;
import com.furniture.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/users/profile")
    public ResponseEntity<User> createUserHandler(
            @RequestHeader("Authorization") String jwt
            ) throws Exception {

        User user = userService.findUserByJwtToken(jwt);


        return ResponseEntity.ok(user);
    }
}
