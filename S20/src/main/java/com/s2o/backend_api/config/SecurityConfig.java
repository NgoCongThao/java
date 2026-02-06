package com.s2o.backend_api.config;

import com.s2o.backend_api.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // 1. CHO PHÉP TẤT CẢ (PUBLIC)
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/guest/**",
                                "/api/chat/**",
                                "/api/reviews/**",
                                "/error",
                                // Các file static frontend
                                "/*.html", "/img/**", "/css/**", "/js/**"
                        ).permitAll()

                        // 2. QUYỀN ADMIN (Vào đâu cũng được - GOD MODE)
                        .requestMatchers("/api/admin/**").hasAuthority("ADMIN")

                        // 3. QUYỀN STAFF & MANAGER (Dùng hasAnyAuthority để bắt đúng chữ STAFF)
                        // Lưu ý: Thầy thêm ADMIN vào đây để em dùng tk Admin test Staff cũng được luôn
                        .requestMatchers("/api/staff/**").hasAnyAuthority("STAFF", "MANAGER", "ADMIN")
                        //.requestMatchers("/api/staff/**").permitAll()
                        .requestMatchers("/api/kitchen/**").hasAnyAuthority("KITCHEN", "MANAGER", "ADMIN")
                        .requestMatchers("/api/manager/**").hasAnyAuthority("MANAGER", "ADMIN")

                        // 4. QUYỀN CHUNG (ORDER, PROFILE...)
                        // Ai đăng nhập rồi cũng dùng được (Kể cả USER, STAFF, KITCHEN...)
                        .requestMatchers("/api/orders/**").authenticated()
                        .requestMatchers("/api/auth/me").authenticated()

                        // 5. CHỐT CHẶN CUỐI CÙNG
                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // CẤU HÌNH CORS (Cho phép Frontend gọi vào)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*")); // Chấp nhận mọi nguồn (Frontend)
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
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}