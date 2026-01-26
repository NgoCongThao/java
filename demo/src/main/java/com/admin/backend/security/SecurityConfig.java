package com.admin.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public JwtUtil jwtUtil() {
        return new JwtUtil();
    }

    @Bean
    public JwtFilter jwtFilter(JwtUtil jwtUtil) {
        return new JwtFilter(jwtUtil);
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            JwtFilter jwtFilter
    ) throws Exception {

        http
            .cors(cors -> {})
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                // ✅ CHO PHÉP LOGIN KHÔNG CẦN TOKEN
                .requestMatchers("/api/admin/login").permitAll()

                // ✅ PROFILE: CHỈ CẦN ĐÃ LOGIN (authenticated)
                .requestMatchers("/api/admin/profile").authenticated()

                // 🔒 CÁC API KHÁC CẦN LOGIN
                .anyRequest().authenticated()
            )
            // ⭐ GẮN JWT FILTER
            .addFilterBefore(
                jwtFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}