package com.s2o.backend_api.controller;

import com.s2o.backend_api.dto.LoginRequest;
import com.s2o.backend_api.dto.RegisterRequest;
import com.s2o.backend_api.entity.User;
import com.s2o.backend_api.repository.UserRepository;
import com.s2o.backend_api.utils.JwtUtils; // <--- 1. Import JwtUtils
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils; // <--- 2. Tiêm công cụ tạo Token

    // --- 1. API ĐĂNG KÝ ---
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
        // Kiểm tra trùng tên đăng nhập
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("Lỗi: Tên đăng nhập đã tồn tại!");
        }

        // Tạo user mới
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword()); // Lưu ý: Thực tế nên mã hóa password bằng BCrypt sau này
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setRole("USER");

        userRepository.save(user);

        return ResponseEntity.ok("Đăng ký thành công!");
    }

    // --- 2. API ĐĂNG NHẬP (Đã nâng cấp JWT) ---
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest request) {
        // Tìm user trong DB
        Optional<User> userOptional = userRepository.findByUsername(request.getUsername());

        // Kiểm tra xem có user không VÀ mật khẩu có khớp không
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getPassword().equals(request.getPassword())) {
                
                // --- 3. TẠO TOKEN JWT ---
                String token = jwtUtils.generateToken(user.getUsername());

                // Đóng gói dữ liệu trả về (Token + Info user)
                Map<String, Object> response = new HashMap<>();
                response.put("token", token);       // <--- QUAN TRỌNG NHẤT
                response.put("type", "Bearer");
                response.put("id", user.getId());
                response.put("username", user.getUsername());
                response.put("fullName", user.getFullName());
                response.put("role", user.getRole());
                
                return ResponseEntity.ok(response);
            }
        }

        // Đăng nhập thất bại
        return ResponseEntity.badRequest().body("Sai tài khoản hoặc mật khẩu!");
    }
}