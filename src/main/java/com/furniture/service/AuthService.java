package com.furniture.service;

import com.furniture.domain.USER_ROLE;
import com.furniture.request.LoginRequest;
import com.furniture.response.AuthResponse;
import com.furniture.response.SignupRequest;

public interface AuthService {

    void sentLoginOtp(String email, USER_ROLE role) throws Exception;
    String createUser(SignupRequest req) throws Exception;

    AuthResponse signing(LoginRequest req);
}
