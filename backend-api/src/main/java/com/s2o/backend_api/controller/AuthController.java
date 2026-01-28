
package com.s2o.backend_api.controller;

import com.s2o.backend_api.dto.LoginRequest;
import com.s2o.backend_api.dto.RegisterRequest;
import com.s2o.backend_api.entity.User;
import com.s2o.backend_api.repository.UserRepository;
import com.s2o.backend_api.utils.JwtUtils;
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
    private JwtUtils jwtUtils;

    // --- 1. API ĐĂNG KÝ ---
    // ... các import giữ nguyên

@PostMapping("/register")
public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
    if (userRepository.existsByUsername(request.getUsername())) {
        return ResponseEntity.badRequest().body("Lỗi: Tên đăng nhập đã tồn tại!");
    }

    User user = new User();
    user.setUsername(request.getUsername());
    user.setPassword(request.getPassword()); // TODO: sau này nên BCrypt
    user.setFullName(request.getFullName());
    user.setPhone(request.getPhone());
    
    // THÊM: phân biệt role dựa trên restaurantId
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

@PostMapping("/login")
public ResponseEntity<?> loginUser(@RequestBody LoginRequest request) {
    Optional<User> userOptional = userRepository.findByUsername(request.getUsername());

    if (userOptional.isPresent()) {
        User user = userOptional.get();
        if (user.getPassword().equals(request.getPassword())) {
            String token = jwtUtils.generateToken(user.getUsername());

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("type", "Bearer");
            response.put("id", user.getId());
            response.put("username", user.getUsername());
            response.put("fullName", user.getFullName());
            response.put("role", user.getRole());

            // THÊM: trả về restaurantId (null nếu là khách thường)
            response.put("restaurantId", user.getRestaurantId());

            response.put("phone", user.getPhone());
            response.put("address", user.getAddress());

            return ResponseEntity.ok(response);
        }
    }
    return ResponseEntity.badRequest().body("Sai tài khoản hoặc mật khẩu!");
}

// ... phần change-password giữ nguyên
    // --- 3. API ĐỔI MẬT KHẨU (MỚI THÊM) ---
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        // Tìm user theo ID
        Optional<User> userOpt = userRepository.findById(request.getUserId());
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("User không tồn tại!");
        }

        User user = userOpt.get();

        // Kiểm tra mật khẩu cũ (So sánh chuỗi thô vì hiện tại bạn chưa mã hóa pass)
        if (!user.getPassword().equals(request.getOldPassword())) {
            return ResponseEntity.badRequest().body("Mật khẩu cũ không đúng!");
        }

        // Cập nhật mật khẩu mới
        user.setPassword(request.getNewPassword());
        userRepository.save(user);

        return ResponseEntity.ok("Đổi mật khẩu thành công!");
    }
}

// --- DTO PHỤ TRỢ (Để hứng dữ liệu đổi pass) ---
class ChangePasswordRequest {
    private Long userId;
    private String oldPassword;
    private String newPassword;

    // Getters & Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getOldPassword() { return oldPassword; }
    public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}