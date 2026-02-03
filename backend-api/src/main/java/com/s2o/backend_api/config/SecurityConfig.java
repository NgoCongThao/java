package com.s2o.backend_api.config;

import com.s2o.backend_api.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity; // Thêm dòng này
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // Dùng BCrypt
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Kích hoạt @PreAuthorize trong Controller
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CẤU HÌNH CORS Ở ĐÂY
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // --- 1. CÁC API PUBLIC (AI CŨNG GỌI ĐƯỢC) ---
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/guest/**",        // <-- Quan trọng: Để Admin load danh sách nhà hàng
                                "/api/chat/**",         // Chatbot
                                "/api/reviews/**",      // Xem review

                                // Các file tĩnh frontend
                                "/landing.html", "/authcus.html",
                                "/admin-login.html", "/admin.html",
                                "/kitchen.html", "/staff.html",
                                "/img/**", "/css/**", "/js/**"
                        ).permitAll()

                        // --- 2. PHÂN QUYỀN API (ROLE-BASED) ---
                        .requestMatchers("/api/admin/**").hasAuthority("ADMIN")       // Chỉ Admin hệ thống
                        .requestMatchers("/api/manager/**").hasAuthority("MANAGER")   // Chỉ Chủ quán
                        .requestMatchers("/api/kitchen/**").hasAnyAuthority("KITCHEN", "MANAGER") // Bếp & Chủ
                        .requestMatchers("/api/staff/**").hasAnyAuthority("STAFF", "MANAGER")     // NV & Chủ

                        // --- 3. CÒN LẠI PHẢI ĐĂNG NHẬP ---
                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // --- FIX LỖI CORS (QUAN TRỌNG) ---
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Cho phép tất cả các nguồn (hoặc cụ thể: "http://127.0.0.1:5500")
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));

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
        // QUAY LẠI BCRYPT ĐỂ BẢO MẬT & ĐĂNG NHẬP ĐƯỢC ADMIN
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}