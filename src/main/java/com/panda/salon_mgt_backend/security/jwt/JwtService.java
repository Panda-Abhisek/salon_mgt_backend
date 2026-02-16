package com.panda.salon_mgt_backend.security.jwt;

import com.panda.salon_mgt_backend.models.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Data
@Slf4j
public class JwtService {

    private final SecretKey key;
    private final long accessTtlSeconds;
    private final long refreshTtlSeconds;
    private final String issuer;

    @Value("${security.jwt.secret}") String secret;

    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.access-ttl-seconds}") long accessTtlSeconds,
            @Value("${security.jwt.refresh-ttl-seconds}") long refreshTtlSeconds,
            @Value("${security.jwt.issuer}") String issuer
    ) {
        if (secret == null || secret.length() < 64) {
            throw new IllegalArgumentException("JWT secret must be at least 64 characters long");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTtlSeconds = accessTtlSeconds;
        this.refreshTtlSeconds = refreshTtlSeconds;
        this.issuer = issuer;
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();

        List<String> roles = user.getRoles()
                .stream()
                .map(r -> r.getRoleName().name())
                .toList();

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getEmail()) // ✅ FIX
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessTtlSeconds)))
                .claims(Map.of(
                        "userId", user.getUserId(),
                        "roles", roles,
                        "typ", "access"
                ))
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(User user, String jti) {
        Instant now = Instant.now();
        return Jwts.builder()
                .id(jti)
                .subject(user.getEmail())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(refreshTtlSeconds)))
                .claim("typ", "refresh")
                .signWith(key)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            Claims claims = parse(token).getPayload();
            String usernameFromToken = claims.getSubject(); // email
            Date expiration = claims.getExpiration();

            return usernameFromToken.equals(userDetails.getUsername())
                    && expiration.after(new Date());

        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // parse the token
    public Jws<Claims> parse(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
    }

    public boolean isAccessToken(String token) {
        try {
            Claims claims = parse(token).getPayload();
            return "access".equals(claims.get("typ", String.class));
        } catch (ExpiredJwtException e) {
            // 🔥 Token is expired but we can still read its claims
            Claims claims = e.getClaims();
            return "access".equals(claims.get("typ", String.class));
        }
    }

    public boolean isRefreshToken(String token) {
        Claims c = parse(token).getPayload();
        return "refresh".equals(c.get("typ", String.class));
    }

    public String getJti(String token) {
        Claims c = parse(token).getPayload();
        return c.getId();
    }

    public String extractUsername(String token) {
        try {
            return parse(token).getPayload().getSubject();
        } catch (ExpiredJwtException e) {
            return e.getClaims().getSubject();
        }
    }

}
