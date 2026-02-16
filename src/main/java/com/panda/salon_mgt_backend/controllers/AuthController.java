package com.panda.salon_mgt_backend.controllers;

import com.panda.salon_mgt_backend.payloads.RefreshTokenRequest;
import com.panda.salon_mgt_backend.payloads.TokenResponse;
import com.panda.salon_mgt_backend.payloads.UserResponse;
import com.panda.salon_mgt_backend.security.requests.UserLoginRequest;
import com.panda.salon_mgt_backend.security.requests.UserRegisterRequest;
import com.panda.salon_mgt_backend.security.responses.MessageResponse;
import com.panda.salon_mgt_backend.services.AuthService;
import com.panda.salon_mgt_backend.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> registerUser(@RequestBody UserRegisterRequest userRegisterRequest) {
        return authService.registerUser(userRegisterRequest);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody UserLoginRequest loginRequest, HttpServletResponse response) {
        return ResponseEntity.ok(authService.loginUser(loginRequest, response));
    }

    //access and refresh token renew karne lie lie api
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(
            @RequestBody(required = false) RefreshTokenRequest body,
            HttpServletResponse response,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(authService.refreshTokenService(body, response, request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logoutUser(request, response);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(Authentication authentication) {
        UserResponse response = userService.getCurrentUser(authentication);
        return ResponseEntity.ok(response);
    }

}
