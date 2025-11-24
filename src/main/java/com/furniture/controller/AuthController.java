// File: src/main/java/com/furniture/controller/AuthController.java
package com.furniture.controller;

import com.furniture.domain.USER_ROLE;
import com.furniture.request.LoginOtpRequest;
import com.furniture.request.LoginRequest;
import com.furniture.request.SignupRequest;
import com.furniture.response.ApiResponse;
import com.furniture.response.AuthResponse;
import com.furniture.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.furniture.config.JwtProvider; // Import thêm
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // Import thêm
import org.springframework.security.core.Authentication; // Import thêm
import org.springframework.security.core.userdetails.UserDetails; // Import thêm
import com.furniture.service.impl.CustomUserServiceImpl; // Import thêm

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // Inject thêm để xử lý refresh token
    private final JwtProvider jwtProvider;
    private final CustomUserServiceImpl customUserService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> createUserHandler(@RequestBody SignupRequest req) throws Exception {
        // Hàm này trả về String jwt, cần sửa lại AuthService để trả về AuthResponse đầy đủ
        // Hoặc tạm thời chỉ set jwt (access token)
        String jwt = authService.createUser(req);

        AuthResponse res = new AuthResponse();
        res.setJwt(jwt);
        res.setMessage("register success");
        res.setRole(USER_ROLE.ROLE_CUSTOMER);

        return ResponseEntity.ok(res);
    }

    @PostMapping("/sent/login-signup-otp")
    public ResponseEntity<ApiResponse> sentOtpHandler(@RequestBody LoginOtpRequest req) throws Exception {
        authService.sentLoginOtp(req.getEmail(), req.getRole());
        ApiResponse res = new ApiResponse();
        res.setMessage("otp sent successfully");
        return ResponseEntity.ok(res);
    }

    @PostMapping("/signing")
    public ResponseEntity<AuthResponse> loginHandler(@RequestBody LoginRequest req) throws Exception {
        // AuthResponse này giờ sẽ chứa cả refreshToken (do AuthServiceImpl cập nhật)
        AuthResponse authResponse = authService.signing(req);
        return ResponseEntity.ok(authResponse);
    }

    // --- API MỚI: Refresh Token ---
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshTokenHandler(@RequestBody AuthResponse req) throws Exception {
        String refreshToken = req.getRefreshToken();
        String email = jwtProvider.getEmailFromToken(refreshToken);

        UserDetails userDetails = customUserService.loadUserByUsername(email);

        // Tạo Authentication object
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );

        // Cấp Access Token mới
        String newAccessToken = jwtProvider.generateAccessToken(authentication);

        AuthResponse res = new AuthResponse();
        res.setJwt(newAccessToken);
        res.setRefreshToken(refreshToken); // Trả lại refresh token cũ
        res.setMessage("Token refreshed successfully");

        return ResponseEntity.ok(res);
    }
}