/* package com.admin.backend.filter;

import com.admin.backend.util.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TenantFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            // 👉 Lấy tenant từ HEADER
            String tenantHeader = request.getHeader("X-Tenant-ID");

            if (tenantHeader != null && !tenantHeader.isEmpty()) {
                Long tenantId = Long.parseLong(tenantHeader);
                TenantContext.setTenantId(tenantId);
            } else {
                // tuỳ bạn: reject hoặc set default
                throw new RuntimeException("Missing X-Tenant-ID header");
            }

            filterChain.doFilter(request, response);

        } finally {
            // 🚨 cực kỳ quan trọng
            TenantContext.clear();
        }
    }
} */