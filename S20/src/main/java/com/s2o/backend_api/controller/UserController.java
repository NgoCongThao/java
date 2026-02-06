package com.s2o.backend_api.controller;

import com.s2o.backend_api.entity.User;
import com.s2o.backend_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api") 
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // --- CÁC API DÀNH CHO USER THƯỜNG (Profile) ---
    // (Giữ nguyên phần này cho User sử dụng)

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

    // ❌ ĐÃ XÓA: getAllUsers (Đã có bên AdminController)
    // ❌ ĐÃ XÓA: deleteUser (Đã có bên AdminController)
    // ❌ ĐÃ XÓA: updateUserByAdmin (Đã có bên AdminController)
}