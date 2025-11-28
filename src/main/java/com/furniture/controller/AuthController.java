package com.furniture.controller;

import java.util.Collections;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.furniture.config.JwtProvider;
import com.furniture.domain.USER_ROLE;
import com.furniture.modal.Seller;
import com.furniture.modal.User;
import com.furniture.repository.SellerRepository;
import com.furniture.repository.UserRepository;
import com.furniture.request.LoginRequest;
import com.furniture.request.SignupRequest;
import com.furniture.response.ApiResponse;
import com.furniture.response.AuthResponse;
import com.furniture.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final SellerRepository sellerRepository;

    // Các endpoint hiện có của bạn...

    @PostMapping("/sent/login-signup-otp")
    public ResponseEntity<ApiResponse> sentOtpHandler(@RequestBody Map<String, String> request) throws Exception {
        String email = request.get("email");
        String roleStr = request.get("role");

        USER_ROLE role = null;
        if (roleStr != null && !roleStr.isEmpty()) {
            role = USER_ROLE.valueOf(roleStr);
        }

        authService.sentLoginOtp(email, role);

        ApiResponse res = new ApiResponse();
        res.setMessage("OTP sent successfully");
        return ResponseEntity.ok(res);
    }

    @PostMapping("/signing")
    public ResponseEntity<AuthResponse> loginHandler(@Valid @RequestBody LoginRequest req) throws Exception {
        AuthResponse authResponse = authService.signing(req);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> createUserHandler(@Valid @RequestBody SignupRequest req) throws Exception {
        String jwt = authService.createUser(req);

        AuthResponse res = new AuthResponse();
        res.setJwt(jwt);
        res.setMessage("Register success");
        res.setRole(USER_ROLE.ROLE_CUSTOMER);

        return new ResponseEntity<>(res, HttpStatus.CREATED);
    }

    // ===== ENDPOINT MỚI: REFRESH TOKEN =====
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refreshToken");

            if (refreshToken == null || refreshToken.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Refresh token is required"));
            }

            // 1. Validate refresh token
            if (!jwtProvider.validateToken(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid or expired refresh token"));
            }

            // 2. Kiểm tra xem có phải refresh token không (không phải access token)
            if (!jwtProvider.isRefreshToken(refreshToken)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Token is not a refresh token"));
            }

            // 3. Lấy email từ token
            String email = jwtProvider.getEmailFromToken(refreshToken);

            // 4. Tìm user/seller và tạo Authentication
            User user = userRepository.findByEmail(email);
            Seller seller = null;
            USER_ROLE role = USER_ROLE.ROLE_CUSTOMER;

            if (user == null) {
                // Thử tìm trong bảng Seller
                seller = sellerRepository.findByEmail(email);
                if (seller == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(Map.of("error", "User not found"));
                }
                role = USER_ROLE.ROLE_SELLER;
            } else {
                role = user.getRole();
            }

            // 5. Tạo Authentication object
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    email,
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority(role.toString()))
            );

            // 6. Tạo access token mới
            String newAccessToken = jwtProvider.generateAccessToken(authentication);

            // 7. Tùy chọn: Có thể tạo refresh token mới (Rotating Refresh Token - Bảo mật cao hơn)
            // String newRefreshToken = jwtProvider.generateRefreshToken(authentication);

            // 8. Trả về response
            AuthResponse response = new AuthResponse();
            response.setJwt(newAccessToken);
            response.setRefreshToken(refreshToken); // Giữ nguyên refresh token cũ
            // response.setRefreshToken(newRefreshToken); // Hoặc dùng refresh token mới
            response.setMessage("Token refreshed successfully");
            response.setRole(role);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to refresh token: " + e.getMessage()));
        }
    }
}