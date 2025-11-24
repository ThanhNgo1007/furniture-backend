// File: src/main/java/com/furniture/response/AuthResponse.java
package com.furniture.response;

import com.furniture.domain.USER_ROLE;
import lombok.Data;

@Data
public class AuthResponse {
    private String jwt;          // Access Token
    private String refreshToken; // Refresh Token (Thêm mới)
    private String message;
    private USER_ROLE role;
}