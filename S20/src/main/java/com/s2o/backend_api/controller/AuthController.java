package com.s2o.backend_api.controller;

import com.s2o.backend_api.dto.LoginRequest;
import com.s2o.backend_api.dto.RegisterRequest;
import com.s2o.backend_api.entity.User;
import com.s2o.backend_api.repository.UserRepository;
import com.s2o.backend_api.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
// --- THÊM IMPORT ĐỂ SỬA LỖI ---
import org.springframework.security.core.userdetails.UserDetails;
import java.util.ArrayList; 
// -----------------------------
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
    private JwtUtils jwtUtils;

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // --- 1. API ĐĂNG KÝ ---
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("Lỗi: Tên đăng nhập đã tồn tại!");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword())); 
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        
        if (request.getRestaurantId() != null) {
            user.setRole("KITCHEN");
            user.setRestaurantId(request.getRestaurantId());
        } else {
            user.setRole("USER");
        }

        userRepository.save(user);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Đăng ký thành công!");
        return ResponseEntity.ok(response);
    }

    // --- 2. API ĐĂNG NHẬP (ĐÃ SỬA LỖI generateToken) ---
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest request) {
        Optional<User> userOptional = userRepository.findByUsername(request.getUsername());

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                
                // --- SỬA LỖI TẠI ĐÂY ---
                // JwtUtils cần UserDetails, nên ta tạo nhanh một cái từ user tìm được
                UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                        user.getUsername(), 
                        user.getPassword(), 
                        new ArrayList<>() // Tạm thời không cần role trong UserDetails
                );

                // Bây giờ truyền userDetails vào sẽ không bị lỗi nữa
                String token = jwtUtils.generateToken(userDetails);
                // -----------------------

                Map<String, Object> response = new HashMap<>();
                response.put("token", token);
                response.put("type", "Bearer");
                response.put("id", user.getId());
                response.put("username", user.getUsername());
                response.put("fullName", user.getFullName());
                response.put("role", user.getRole());
                response.put("restaurantId", user.getRestaurantId());
                response.put("phone", user.getPhone());
                response.put("address", user.getAddress());

                return ResponseEntity.ok(response);
            }
        }

        return ResponseEntity.badRequest().body("Sai tài khoản hoặc mật khẩu!");
    }

    // --- 3. API ĐỔI MẬT KHẨU ---
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        Optional<User> userOpt = userRepository.findById(request.getUserId());
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("User không tồn tại!");
        }

        User user = userOpt.get();

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body("Mật khẩu cũ không đúng!");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return ResponseEntity.ok("Đổi mật khẩu thành công!");
    }
}

// DTO
class ChangePasswordRequest {
    private Long userId;
    private String oldPassword;
    private String newPassword;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getOldPassword() { return oldPassword; }
    public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}