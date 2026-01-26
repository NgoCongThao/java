package com.admin.backend.controller;

import com.admin.backend.entity.User;
import com.admin.backend.repository.UserRepository;
import com.admin.backend.security.JwtUtil;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AuthController {

    private final UserRepository userRepo;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepo, JwtUtil jwtUtil) {
        this.userRepo = userRepo;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public String login(@RequestBody User req) {

        User user = userRepo.findByUsername(req.getUsername())
                .orElseThrow(() -> new RuntimeException("Sai tài khoản"));

        // ✅ SO SÁNH CHUỖI THƯỜNG
        if (!user.getPassword().equals(req.getPassword())) {
            throw new RuntimeException("Sai mật khẩu");
        }

        return jwtUtil.generateToken(
                user.getId(),
                user.getTenantId(),
                user.getRole().name()
        );
    }
}