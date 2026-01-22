package com.s2o.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Collections;

public class SimpleAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Lấy token từ Header gửi lên
        String header = request.getHeader("Authorization");

        // 2. Kiểm tra xem có phải token "fake" của mình không
        // Format: "Bearer fake-jwt-token-USERNAME"
        if (header != null && header.startsWith("Bearer fake-jwt-token-")) {

            // 3. Cắt chuỗi để lấy username (Bỏ phần đầu đi)
            String username = header.substring("Bearer fake-jwt-token-".length());

            if (!username.isEmpty()) {
                // 4. Báo cho Spring Security biết user này đã đăng nhập
                // (Tạm thời gán quyền ROLE_USER cho nhanh)
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        // 5. Cho phép request đi tiếp vào Controller
        filterChain.doFilter(request, response);
    }
}