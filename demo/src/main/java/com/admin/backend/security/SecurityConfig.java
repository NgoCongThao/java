package com.admin.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
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

    // ✅ BẮT BUỘC: cho CORS preflight
    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

    // ✅ LOGIN: không cần token
    .requestMatchers("/api/admin/login").permitAll()

    // ✅ PROFILE: chỉ cần login
    .requestMatchers("/api/admin/profile").authenticated()

    // 🔒 ADMIN API: CẦN ROLE MANAGER
    .requestMatchers("/api/admin/**").hasRole("MANAGER")

    // ❌ còn lại chặn hết
    .anyRequest().denyAll()
        )
            .addFilterBefore(
                jwtFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}