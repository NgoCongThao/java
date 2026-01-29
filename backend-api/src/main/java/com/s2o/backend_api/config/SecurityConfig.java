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
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    // ... các import và phần đầu file giữ nguyên ...

// ... phần đầu giữ nguyên

@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .cors(cors -> cors.configure(http))
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/api/auth/**", 
                "/api/guest/**", 
                "/api/reviews/**", 
                "/api/bookings/**",
                "/api/bookings/create",
                
                // --- CÁC TRANG HTML ĐƯỢC PHÉP TRUY CẬP ---
                "/landing.html", 
                "/authcus.html", 
                "/admin-login.html",
                "/kitchen-auth.html",  // <--- THÊM DÒNG NÀY (Trang đăng nhập bếp)
                "/kitchen.html",       // <--- THÊM DÒNG NÀY (Trang giao diện bếp)
                
                "/img/**", "/css/**", "/js/**"
            ).permitAll()
            // Nếu muốn kitchen public tạm thời để test (không khuyến khích lâu dài)
            // .requestMatchers("/api/kitchen/**").permitAll()
            .anyRequest().authenticated()
        )
        .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
}
// ... phần còn lại của file giữ nguyên ...

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Tạm thời chưa mã hóa password để dễ test
        return NoOpPasswordEncoder.getInstance(); 
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}