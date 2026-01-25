package com.admin.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {


    private static final String SECRET_KEY =
            "SECRET_KEY_123456_SECRET_KEY_123456";

    private static final SecretKey KEY =
            Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));

   
    public String generateToken(Long userId, String role, Long tenantId) {
        return Jwts.builder()
                .setClaims(Map.of(
                        "userId", userId,
                        "role", role,
                        "tenantId", tenantId
                ))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 1 ngày
                .signWith(KEY) // ✅ API mới
                .compact();
    }


    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()   // ✅ API mới
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}