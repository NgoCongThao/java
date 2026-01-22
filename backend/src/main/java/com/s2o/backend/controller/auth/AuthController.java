package com.s2o.backend.controller.auth;

import com.s2o.backend.entity.User;
import com.s2o.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // "Chìa khóa" để đăng nhập staff1

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        // 1. Tìm user
        Optional<User> userOptional = userRepository.findByUsername(loginRequest.getUsername());

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(401).body("Tài khoản không tồn tại!");
        }

        User user = userOptional.get();

        // 2. SO SÁNH MẬT KHẨU (Quan trọng nhất)
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body("Sai mật khẩu!");
        }

        // 3. Trả về thông tin
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("username", user.getUsername());
        response.put("fullName", user.getFullName());
        response.put("role", user.getRole());

        if (user.getRestaurant() != null) {
            response.put("restaurantId", user.getRestaurant().getId());
        }

        response.put("token", "fake-jwt-token-" + user.getUsername());
        return ResponseEntity.ok(response);
    }
}

// Class phụ để hứng dữ liệu
class LoginRequest {
    private String username;
    private String password;
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}