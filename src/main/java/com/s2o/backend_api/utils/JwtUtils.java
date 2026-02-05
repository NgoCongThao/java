package com.s2o.backend_api.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority; // Thêm dòng này
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap; // Thêm dòng này
import java.util.Map;     // Thêm dòng này
import java.util.function.Function;

@Component
public class JwtUtils {
    // Khóa bí mật (Giữ nguyên)
    public static final String SECRET = "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";

    // 1. Tạo Token (SỬA LẠI: Nhận UserDetails thay vì String username)
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        
        // --- ĐOẠN QUAN TRỌNG MỚI THÊM ---
        // Lấy quyền (Role) từ UserDetails và đưa vào claims
        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("USER");
        claims.put("role", role); 
        // --------------------------------

        return createToken(claims, userDetails.getUsername());
    }

    // Hàm hỗ trợ tạo token với claims (MỚI THÊM)
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims) // Đưa role vào token
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 24 giờ
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 2. Lấy Username từ Token (Giữ nguyên)
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // 3. Kiểm tra Token có hợp lệ không (Giữ nguyên)
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    // Các hàm hỗ trợ bên dưới (Giữ nguyên)
    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token).getBody();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}