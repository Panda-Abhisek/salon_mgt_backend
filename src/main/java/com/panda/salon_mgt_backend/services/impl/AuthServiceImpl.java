package com.panda.salon_mgt_backend.services.impl;

import com.panda.salon_mgt_backend.exceptions.RefreshTokenException;
import com.panda.salon_mgt_backend.models.AppRole;
import com.panda.salon_mgt_backend.models.RefreshToken;
import com.panda.salon_mgt_backend.models.Role;
import com.panda.salon_mgt_backend.models.User;
import com.panda.salon_mgt_backend.payloads.RefreshTokenRequest;
import com.panda.salon_mgt_backend.payloads.TokenResponse;
import com.panda.salon_mgt_backend.repositories.RefreshTokenRepository;
import com.panda.salon_mgt_backend.repositories.RoleRepository;
import com.panda.salon_mgt_backend.repositories.UserRepository;
import com.panda.salon_mgt_backend.security.jwt.JwtService;
import com.panda.salon_mgt_backend.security.requests.UserLoginRequest;
import com.panda.salon_mgt_backend.security.requests.UserRegisterRequest;
import com.panda.salon_mgt_backend.security.responses.MessageResponse;
import com.panda.salon_mgt_backend.security.services.CookieService;
import com.panda.salon_mgt_backend.security.services.UserDetailsImpl;
import com.panda.salon_mgt_backend.services.AuthService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CookieService cookieService;
    private final ModelMapper modelMapper;

    @Override
    public ResponseEntity<MessageResponse> registerUser(UserRegisterRequest request) {
        System.out.println("username: " + request.getUsername() + ", email: " + request.getEmail() + ", password: " + request.getPassword());
        // validate if user with the same username or email already exists
        if (userRepository.existsByUserName(request.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
        }

        Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));

//        String username = request.getEmail().split("@")[0];

        User user = User.builder()
                .userName(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // In real application, password should be hashed
                .enabled(true)
                .roles(Set.of(userRole))
                .build();

        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("User Registered Successfully!!"));
    }

    @Override
    public TokenResponse loginUser(UserLoginRequest loginRequest, HttpServletResponse response) {
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        assert userDetails != null;
        User user = userRepository.findByEmailWithRoles(userDetails.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password | User not found"));

        if(!userDetails.isEnabled()) {
            throw new DisabledException("User account is disabled");
        }

        // Create and persist refresh token (jti tracked)
        String jti = UUID.randomUUID().toString();
        RefreshToken rt = RefreshToken.builder()
                .jti(jti)
                .user(user)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds()))
                .revoked(false)
                .build();
        refreshTokenRepository.save(rt);

        String accessToken = jwtService.generateAccessToken(user);

        String refreshToken = jwtService.generateRefreshToken(user, jti);

        cookieService.attachRefreshCookie(response, refreshToken, (int) jwtService.getRefreshTtlSeconds());
        cookieService.addNoStoreHeaders(response);

        return TokenResponse.of(accessToken, jwtService.getAccessTtlSeconds());
    }

    @Override
    public void logoutUser(HttpServletRequest request, HttpServletResponse response) {
        readRefreshTokenFromRequest(null, request).ifPresent(token -> {
            try {
                if (jwtService.isRefreshToken(token)) {
                    String jti = jwtService.getJti(token);
                    refreshTokenRepository.findByJti(jti).ifPresent(rt -> {
                        rt.setRevoked(true);
                        refreshTokenRepository.save(rt);
                    });
                }
            } catch (JwtException ignored) {
            }
        });

        // Use CookieUtil (same behavior)
        cookieService.clearRefreshCookie(response);
        cookieService.addNoStoreHeaders(response);
        SecurityContextHolder.clearContext();
    }

    @Override
    public TokenResponse refreshTokenService(
            RefreshTokenRequest body,
            HttpServletResponse response,
            HttpServletRequest request
    ) {

        String refreshToken = readRefreshTokenFromRequest(body, request)
                .orElseThrow(() -> {
                    cookieService.clearRefreshCookie(response);
                    return new RefreshTokenException("Refresh token is missing");
                });

        if (!jwtService.isRefreshToken(refreshToken)) {
            cookieService.clearRefreshCookie(response);
            throw new RefreshTokenException("Invalid refresh token type");
        }
        String jti = jwtService.getJti(refreshToken);

        RefreshToken storedRefreshToken = refreshTokenRepository
                .findByJti(jti)
                .orElseThrow(() -> {
                    cookieService.clearRefreshCookie(response);
                    return new RefreshTokenException("Refresh token not recognized");
                });

        Long userId = storedRefreshToken.getUser().getUserId();

        if (storedRefreshToken.isRevoked()) {
            cookieService.clearRefreshCookie(response);
            throw new RefreshTokenException("Refresh token revoked");
        }

        if (storedRefreshToken.getExpiresAt().isBefore(Instant.now())) {
            cookieService.clearRefreshCookie(response);
            throw new RefreshTokenException("Refresh token expired");
        }

        if (!storedRefreshToken.getUser().getUserId().equals(userId)) {
            cookieService.clearRefreshCookie(response);
            throw new RefreshTokenException("Refresh token does not belong to this user");
        }

        // 🔁 ROTATE OLD TOKEN
        storedRefreshToken.setRevoked(true);
        String newJti = UUID.randomUUID().toString();
        storedRefreshToken.setReplacedByToken(newJti);
        refreshTokenRepository.save(storedRefreshToken);

        // 🔍 Load user WITH roles (fixes LazyInitialization)
        User user = userRepository
                .findByIdWithRoles(userId)
                .orElseThrow(() -> {
                    cookieService.clearRefreshCookie(response);
                    return new RefreshTokenException("User not found");
                });

        // 💾 Store new refresh token
        RefreshToken newRefreshTokenEntity = RefreshToken.builder()
                .jti(newJti)
                .user(user)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds()))
                .revoked(false)
                .build();

        refreshTokenRepository.save(newRefreshTokenEntity);

        // 🔐 Generate tokens
        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(
                user,
                newRefreshTokenEntity.getJti()
        );

        // 🍪 Attach cookie
        cookieService.attachRefreshCookie(
                response,
                newRefreshToken,
                (int) jwtService.getRefreshTtlSeconds()
        );

        cookieService.addNoStoreHeaders(response);

        return TokenResponse.of(
                newAccessToken,
                jwtService.getAccessTtlSeconds()
        );
    }

    //this method will read refresh token from request header or body.
    private Optional<String> readRefreshTokenFromRequest(RefreshTokenRequest body, HttpServletRequest request) {
        // 1. prefer reading refresh token from cookie
        if (request.getCookies() != null) {
            Optional<String> fromCookie = Arrays.stream(request.getCookies())
                    .filter(c -> cookieService.getRefreshTokenCookieName().equals(c.getName()))
                    .map(Cookie::getValue)
                    .filter(v -> !v.isBlank())
                    .findFirst();

            if (fromCookie.isPresent()) {
                return fromCookie;
            }
        }

        // 2 body:
        if (body != null && body.refreshToken() != null && !body.refreshToken().isBlank()) {
            return Optional.of(body.refreshToken());
        }

        //3. custom header
        String refreshHeader = request.getHeader("X-Refresh-Token");
        if (refreshHeader != null && !refreshHeader.isBlank()) {
            return Optional.of(refreshHeader.trim());
        }

        //Authorization = Bearer <token>
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            String candidate = authHeader.substring(7).trim();
            if (!candidate.isEmpty()) {
                try {
                    if (jwtService.isRefreshToken(candidate)) {
                        return Optional.of(candidate);
                    }
                } catch (Exception ignored) {
                }
            }
        }

        return Optional.empty();
    }
}
