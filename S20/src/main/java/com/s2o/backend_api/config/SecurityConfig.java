package com.s2o.backend_api.config;

import com.s2o.backend_api.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // <--- 1. Import BCrypt
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 2. CẤU HÌNH CORS (Cho phép Frontend gọi API)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                
                // --- NHÓM 1: CÔNG KHAI (Ai cũng vào được) ---
                .requestMatchers(
                    "/api/auth/**",          // Đăng nhập/Đăng ký
                    "/api/guest/**",         // Xem menu/nhà hàng
                    "/api/reviews/**",       // Xem review (nếu add review cần login thì tách ra)
                    "/api/chat/**",          // Chat AI
                    "/api/bookings/table-status", // Check bàn trống
                    
                    // Các file HTML/CSS/JS frontend
                    "/landing.html", "/authcus.html", "/admin-login.html", 
                    "/kitchen-auth.html", "/kitchen.html", "/tracking.html",
                    "/img/**", "/css/**", "/js/**", "/"
                ).permitAll()

                // --- NHÓM 2: DÀNH RIÊNG CHO KHÁCH HÀNG (USER) ---
                .requestMatchers(
                    "/api/orders/create",
                    "/api/bookings/create",
                    "/api/bookings/user/**",
                    "/api/orders/my-orders/**"
                ).hasAnyAuthority("USER", "ROLE_USER") 

                // --- NHÓM 3: DÀNH RIÊNG CHO BẾP (KITCHEN) ---
                .requestMatchers(
                    "/api/kitchen/**"
                ).hasAnyAuthority("KITCHEN", "ROLE_KITCHEN", "ADMIN", "ROLE_ADMIN")

                // --- NHÓM 4: DÀNH RIÊNG CHO ADMIN ---
                .requestMatchers(
                    "/api/admin/**"
                ).hasAnyAuthority("ADMIN", "ROLE_ADMIN")

                // Các request còn lại bắt buộc phải đăng nhập
                .anyRequest().authenticated()
            )
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // --- BEAN CORS (Quan trọng để Frontend không lỗi) ---
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*")); // Cho phép tất cả nguồn (HTML file)
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 3. QUAN TRỌNG: Đổi sang BCrypt để khớp với AuthController
        return new BCryptPasswordEncoder(); 
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}