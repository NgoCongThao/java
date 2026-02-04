// package com.s2o.backend_api.controller;

// import com.s2o.backend_api.dto.LoginRequest;
// import com.s2o.backend_api.dto.RegisterRequest;
// import com.s2o.backend_api.entity.User;
// import com.s2o.backend_api.repository.UserRepository;
// import com.s2o.backend_api.utils.JwtUtils; // <--- 1. Import JwtUtils
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import java.util.HashMap;
// import java.util.Map;
// import java.util.Optional;

// @RestController
// @RequestMapping("/api/auth")
// @CrossOrigin(origins = "*")
// public class AuthController {

//     @Autowired
//     private UserRepository userRepository;

//     @Autowired
//     private JwtUtils jwtUtils; // <--- 2. Tiêm công cụ tạo Token

//     // --- 1. API ĐĂNG KÝ ---
//     @PostMapping("/register")
//     public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
//         // Kiểm tra trùng tên đăng nhập
//         if (userRepository.existsByUsername(request.getUsername())) {
//             return ResponseEntity.badRequest().body("Lỗi: Tên đăng nhập đã tồn tại!");
//         }

//         // Tạo user mới
//         User user = new User();
//         user.setUsername(request.getUsername());
//         user.setPassword(request.getPassword()); // Lưu ý: Thực tế nên mã hóa password bằng BCrypt sau này
//         user.setFullName(request.getFullName());
//         user.setPhone(request.getPhone());
//         user.setRole("USER");

//         userRepository.save(user);

//         // SỬA LỖI: Trả về JSON object thay vì String đơn thuần
//         Map<String, String> response = new HashMap<>();
//         response.put("message", "Đăng ký thành công!");
        
//         return ResponseEntity.ok(response);
//     }

//     // --- 2. API ĐĂNG NHẬP (Đã nâng cấp JWT) ---
//     @PostMapping("/login")
//     public ResponseEntity<?> loginUser(@RequestBody LoginRequest request) {
//         // Tìm user trong DB
//         Optional<User> userOptional = userRepository.findByUsername(request.getUsername());

//         // Kiểm tra xem có user không VÀ mật khẩu có khớp không
//         if (userOptional.isPresent()) {
//             User user = userOptional.get();
//             if (user.getPassword().equals(request.getPassword())) {
                
//                 // --- 3. TẠO TOKEN JWT ---
//                 String token = jwtUtils.generateToken(user.getUsername());

//                 // Đóng gói dữ liệu trả về (Token + Info user)
//                 Map<String, Object> response = new HashMap<>();
//                 response.put("token", token);       // <--- QUAN TRỌNG NHẤT
//                 response.put("type", "Bearer");
//                 response.put("id", user.getId());
//                 response.put("username", user.getUsername());
//                 response.put("fullName", user.getFullName());
//                 response.put("role", user.getRole());
                
//                 return ResponseEntity.ok(response);
//             }
//         }

//         // Đăng nhập thất bại
//         return ResponseEntity.badRequest().body("Sai tài khoản hoặc mật khẩu!");
//     }
// }
package com.s2o.backend_api.controller;

import com.s2o.backend_api.dto.LoginRequest;
import com.s2o.backend_api.dto.RegisterRequest;
import com.s2o.backend_api.entity.User;
import com.s2o.backend_api.repository.UserRepository;
import com.s2o.backend_api.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
// --- 1. IMPORT THƯ VIỆN BẢO MẬT ---
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
// ----------------------------------
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

    // --- 2. KHAI BÁO CÔNG CỤ MÃ HÓA PASSWORD ---
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

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
        
        // --- SỬA: MÃ HÓA MẬT KHẨU TRƯỚC KHI LƯU ---
        // Thay vì lưu thô, ta dùng encode()
        user.setPassword(passwordEncoder.encode(request.getPassword())); 
        // ------------------------------------------
        
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setRole("USER");

        userRepository.save(user);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Đăng ký thành công!");
        
        return ResponseEntity.ok(response);
    }

    // --- 2. API ĐĂNG NHẬP ---
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest request) {
        // Tìm user trong DB
        Optional<User> userOptional = userRepository.findByUsername(request.getUsername());

        // Kiểm tra xem có user không VÀ mật khẩu có khớp không
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            
            // --- SỬA: KIỂM TRA MẬT KHẨU ĐÃ MÃ HÓA ---
            // Dùng hàm matches(pass_nhập_vào, pass_trong_db) thay vì equals()
            if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                
                // Tạo Token JWT
                String token = jwtUtils.generateToken(user.getUsername());

                // Đóng gói dữ liệu trả về (Token + Info user)
                Map<String, Object> response = new HashMap<>();
                response.put("token", token);
                response.put("type", "Bearer");
                response.put("id", user.getId());
                response.put("username", user.getUsername());
                response.put("fullName", user.getFullName());
                response.put("role", user.getRole());
                
                // Trả về thêm address và phone để profile hiển thị ngay
                response.put("phone", user.getPhone());
                response.put("address", user.getAddress());
                
                return ResponseEntity.ok(response);
            }
        }

        // Đăng nhập thất bại
        return ResponseEntity.badRequest().body("Sai tài khoản hoặc mật khẩu!");
    }

    // --- 3. API ĐỔI MẬT KHẨU ---
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        // Tìm user theo ID
        Optional<User> userOpt = userRepository.findById(request.getUserId());
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("User không tồn tại!");
        }

        User user = userOpt.get();

        // --- SỬA: KIỂM TRA MẬT KHẨU CŨ (MÃ HÓA) ---
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body("Mật khẩu cũ không đúng!");
        }

        // --- SỬA: MÃ HÓA MẬT KHẨU MỚI ---
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return ResponseEntity.ok("Đổi mật khẩu thành công!");
    }
}

// --- DTO PHỤ TRỢ (GIỮ NGUYÊN) ---
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