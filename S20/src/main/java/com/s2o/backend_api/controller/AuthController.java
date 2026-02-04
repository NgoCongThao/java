package com.s2o.backend_api.controller;

import com.s2o.backend_api.dto.LoginRequest;
import com.s2o.backend_api.dto.RegisterRequest;
import com.s2o.backend_api.entity.User;
import com.s2o.backend_api.repository.UserRepository;
import com.s2o.backend_api.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager; // Thêm dòng này
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // Thêm dòng này
import org.springframework.security.core.Authentication; // Thêm dòng này
import org.springframework.security.core.userdetails.UserDetails; // Thêm dòng này
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

    // Kích hoạt tính năng đăng nhập chuẩn của Spring Security
    @Autowired
    private AuthenticationManager authenticationManager;

    // --- 1. API ĐĂNG KÝ ---
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("Lỗi: Tên đăng nhập đã tồn tại!");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword()); // Pass chưa mã hóa (NoOp)
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        
        // Phân biệt role
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

    // --- 2. API ĐĂNG NHẬP (ĐÃ SỬA LỖI) ---
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest request) {
        try {
            // Bước 1: Để Spring Security tự kiểm tra Username và Password
            // Nếu sai pass, nó sẽ tự ném lỗi (nhảy xuống catch)
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            // Bước 2: Lấy thông tin UserDetails chuẩn từ kết quả đăng nhập
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // Bước 3: Tạo Token (Lúc này hàm generateToken nhận đúng UserDetails)
            String token = jwtUtils.generateToken(userDetails);

            // Bước 4: Lấy thêm thông tin user từ DB để trả về cho Frontend hiển thị
            // (Vì UserDetails chỉ chứa username, pass, role chứ không có fullName, phone...)
            User user = userRepository.findByUsername(request.getUsername()).get();

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("type", "Bearer");
            response.put("id", user.getId());
            response.put("username", user.getUsername());
            response.put("fullName", user.getFullName());
            
            // Lấy role từ userDetails để đảm bảo đúng nhất
            String role = userDetails.getAuthorities().stream().findFirst().get().getAuthority();
            response.put("role", role);

            response.put("restaurantId", user.getRestaurantId());
            response.put("phone", user.getPhone());
            response.put("address", user.getAddress());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Sai tài khoản hoặc mật khẩu!");
        }
    }

    // --- 3. API ĐỔI MẬT KHẨU ---
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        Optional<User> userOpt = userRepository.findById(request.getUserId());
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("User không tồn tại!");
        }

        User user = userOpt.get();

        if (!user.getPassword().equals(request.getOldPassword())) {
            return ResponseEntity.badRequest().body("Mật khẩu cũ không đúng!");
        }

        user.setPassword(request.getNewPassword());
        userRepository.save(user);

        return ResponseEntity.ok("Đổi mật khẩu thành công!");
    }
}

// DTO giữ nguyên
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