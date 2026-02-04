package com.s2o.backend_api.controller;

import com.s2o.backend_api.entity.User;
import com.s2o.backend_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api") // SỬA LẠI: Chỉ để /api, phần còn lại định nghĩa ở từng hàm
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // --- CÁC API DÀNH CHO USER THƯỜNG (Profile) ---

    // 1. Cập nhật địa chỉ
    // URL: /api/users/{id}/address
    @PutMapping("/users/{id}/address")
    public ResponseEntity<?> updateAddress(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String newAddress = body.get("address");
        if (newAddress == null || newAddress.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Địa chỉ không được để trống");
        }
        return userRepository.findById(id).map(user -> {
            user.setAddress(newAddress);
            userRepository.save(user);
            return ResponseEntity.ok("Cập nhật địa chỉ thành công!");
        }).orElse(ResponseEntity.badRequest().body("User không tồn tại"));
    }
    
    // 2. Lấy thông tin user
    // URL: /api/users/{id}
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserProfile(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if(user == null) return ResponseEntity.notFound().build();
        user.setPassword(null); 
        return ResponseEntity.ok(user);
    }

    // --- CÁC API DÀNH CHO ADMIN (Phải khớp với admin.js) ---

    // 3. Xem danh sách tất cả người dùng
    // URL: /api/admin/users (Khớp với admin.js)
    @GetMapping("/admin/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // 4. Xóa người dùng
    // URL: /api/admin/users/{id}
    @DeleteMapping("/admin/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(id);
        return ResponseEntity.ok("Đã xóa người dùng thành công");
    }

    // 5. Sửa thông tin người dùng (Gán role quản lý bếp...)
    // URL: /api/admin/users/{id}
    @PutMapping("/admin/users/{id}")
    public ResponseEntity<?> updateUserByAdmin(@PathVariable Long id, @RequestBody User req) {
        return userRepository.findById(id).map(user -> {
            user.setFullName(req.getFullName());
            user.setRole(req.getRole()); 
            user.setRestaurantId(req.getRestaurantId());
            // Giữ nguyên các trường khác nếu không gửi lên
            if(req.getPhone() != null) user.setPhone(req.getPhone());
            
            userRepository.save(user);
            return ResponseEntity.ok("Cập nhật thông tin user thành công");
        }).orElse(ResponseEntity.notFound().build());
    }
}