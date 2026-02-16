package com.panda.salon_mgt_backend.services;

import com.panda.salon_mgt_backend.payloads.RefreshTokenRequest;
import com.panda.salon_mgt_backend.payloads.TokenResponse;
import com.panda.salon_mgt_backend.security.requests.UserLoginRequest;
import com.panda.salon_mgt_backend.security.requests.UserRegisterRequest;
import com.panda.salon_mgt_backend.security.responses.MessageResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    // register user method
    ResponseEntity<MessageResponse> registerUser(UserRegisterRequest request);

    // login user method
    TokenResponse loginUser(UserLoginRequest request, HttpServletResponse response);

    TokenResponse refreshTokenService(RefreshTokenRequest body, HttpServletResponse response, HttpServletRequest request);

    void logoutUser(HttpServletRequest request, HttpServletResponse response);
}
