package com.panda.salon_mgt_backend.security;

import com.panda.salon_mgt_backend.models.AppRole;
import com.panda.salon_mgt_backend.models.Provider;
import com.panda.salon_mgt_backend.models.RefreshToken;
import com.panda.salon_mgt_backend.models.User;
import com.panda.salon_mgt_backend.repositories.RefreshTokenRepository;
import com.panda.salon_mgt_backend.repositories.RoleRepository;
import com.panda.salon_mgt_backend.repositories.UserRepository;
import com.panda.salon_mgt_backend.security.jwt.JwtService;
import com.panda.salon_mgt_backend.security.services.CookieService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final UserRepository userRepository;
    private final Logger log = LoggerFactory.getLogger(OAuth2SuccessHandler.class);
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CookieService cookieService;
    private final RoleRepository roleRepository;

    @Value("${app.auth.frontend.success-redirect-url}")
    private String frontendSuccessUrl;

//    @Override
//    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
//        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
//        // You can extract user details from oAuth2User and perform necessary actions here
//
//        String registrationId = "UNKNOWN";
//        if(authentication instanceof OAuth2AuthenticationToken token) {
//            registrationId = token.getAuthorizedClientRegistrationId();
//        }
//
//        User user;
//        switch (registrationId) {
//            case "google" -> {
//                String googleId = oAuth2User.getAttributes().getOrDefault("sub", "").toString();
//                String email = oAuth2User.getAttributes().getOrDefault("email", "").toString();
//                String name = oAuth2User.getAttributes().getOrDefault("name", "").toString();
////                String picture = oAuth2User.getAttributes().getOrDefault("picture", "").toString();
//                User newUser = User.builder()
//                        .provider(Provider.GOOGLE)
//                        .email(email)
//                        .userName(name)
//                        .enabled(true)
//                        .providerId(googleId)
//                        .build();
//
//                user = userRepository.findByEmail(email).orElseGet(() -> userRepository.save(newUser));
//            }
//            case "github" -> {
//                String githubId = oAuth2User.getAttributes().getOrDefault("id", "").toString();
//                String name = oAuth2User.getAttributes().getOrDefault("login", "").toString();
//                //String picture = oAuth2User.getAttributes().getOrDefault("avatar_url", "").toString();
//                String email = (String) oAuth2User.getAttributes().get("email");
//                if(email == null || email.isBlank()) {
//                    // GitHub may not provide email if it's private, handle accordingly
//                    email = name + "@github.com"; // Fallback email
//                }
//
//                User newUser = User.builder()
//                        .provider(Provider.GITHUB)
//                        .email(email)
//                        .userName(name)
//                        .providerId(githubId)
//                        .enabled(true)
//                        .build();
//
//                user = userRepository.findByEmail(email).orElseGet(() -> userRepository.save(newUser));
//            }
//            default -> throw new IllegalArgumentException("Unsupported registration ID: " + registrationId);
//        }
//
//        String jti = UUID.randomUUID().toString();
//        RefreshToken refreshTokenOb = RefreshToken.builder()
//                .jti(jti)
//                .user(user)
//                .revoked(false)
//                .createdAt(Instant.now())
//                .expiresAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds()))
//                .build();
//        refreshTokenRepository.save(refreshTokenOb);
//        String accessToken = jwtService.generateAccessToken(user);
//        String refreshToken = jwtService.generateRefreshToken(user, refreshTokenOb.getJti());
//        cookieService.attachRefreshCookie(response, refreshToken, (int) jwtService.getRefreshTtlSeconds());

    /// /        response.getWriter().write("Login Successful. Access Token: " + accessToken);
//        response.sendRedirect(frontendSuccessUrl);
//    }
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String registrationId = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();

        User user;
        String email = extractEmail(registrationId, oAuth2User);

        // 1. Check if user exists first
        user = userRepository.findByEmail(email).orElseGet(() -> {
            // 2. If not, build the new user with default ROLES
            User newUser = User.builder()
                    .email(email)
                    .userName(extractName(registrationId, oAuth2User))
                    .enabled(true)
                    .provider(Provider.valueOf(registrationId.toUpperCase()))
                    .providerId(extractProviderId(registrationId, oAuth2User))
                    // IMPORTANT: You need to inject RoleRepository and find a default role
                    .roles(Set.of(roleRepository.findByRoleName(AppRole.ROLE_USER).get()))
                    .build();
            return userRepository.save(newUser);
        });

        // 3. Refresh Token Logic (Your existing logic is good)
        String jti = UUID.randomUUID().toString();
        RefreshToken refreshTokenOb = RefreshToken.builder()
                .jti(jti)
                .user(user)
                .revoked(false)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds()))
                .build();

        refreshTokenRepository.save(refreshTokenOb);

        // 4. Token Generation
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user, jti);

        // 5. Delivery
        cookieService.attachRefreshCookie(response, refreshToken, (int) jwtService.getRefreshTtlSeconds());

        // REDIRECT with the access token so the frontend can store it
        String targetUrl = frontendSuccessUrl + "?token=" + accessToken;
//        String targetUrl = "http://localhost:5173/oauth/success?token=" + accessToken;
        response.sendRedirect(targetUrl);
    }

    private String extractEmail(String registrationId, OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        if (email == null || email.isBlank()) {
            if ("github".equals(registrationId)) {
                // GitHub users can have private emails
                return oAuth2User.getAttribute("login") + "@github.com";
            }
        }
        return email;
    }

    private String extractName(String registrationId, OAuth2User oAuth2User) {
        if ("github".equals(registrationId)) {
            return oAuth2User.getAttribute("login");
        }
        return oAuth2User.getAttribute("name");
    }

    private String extractProviderId(String registrationId, OAuth2User oAuth2User) {
        if ("google".equals(registrationId)) {
            return oAuth2User.getAttribute("sub"); // Google's unique ID
        }
        // GitHub ID is an Integer, so we convert to String
        Object id = oAuth2User.getAttribute("id");
        return id != null ? id.toString() : null;
    }
}