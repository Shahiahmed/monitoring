package com.example.monitoring.security;

import com.example.monitoring.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final MonitoringJwtProperties properties;
    private SecretKey signingKey;

    @PostConstruct
    void init() {
        String secret = properties.secret();
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException(
                    "monitoring.jwt.secret must be at least 32 characters (256 bits) for HS256");
        }
        signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(User user) {
        long expMs = properties.expirationMs() > 0 ? properties.expirationMs() : 86_400_000L;
        Date now = new Date();
        Date exp = new Date(now.getTime() + expMs);
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .issuedAt(now)
                .expiration(exp)
                .signWith(signingKey)
                .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long parseUserId(String token) {
        String sub = parseClaims(token).getSubject();
        return Long.parseLong(sub);
    }
}
