package com.s2o.backend_api.controller;

import com.s2o.backend_api.entity.User;
import com.s2o.backend_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // --- 1. LẤY USER HIỆN TẠI ---
    @GetMapping("/users/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return ResponseEntity.status(401).body("Chưa đăng nhập");
        }

        String username = auth.getName();
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            return ResponseEntity.status(404).body("Không tìm thấy thông tin người dùng");
        }

        // SỬA LỖI 1: Thêm <String, Object> trước .of
        return ResponseEntity.ok(Map.<String, Object>of(
                "id", user.getId(),
                "username", user.getUsername(),
                "fullName", user.getFullName() != null ? user.getFullName() : user.getUsername(),
                "points", user.getPoints() != null ? user.getPoints() : 0,
                "role", user.getRole() != null ? user.getRole() : "CUSTOMER"
        ));
    }

    // --- 2. CẬP NHẬT ĐỊA CHỈ ---
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

    // --- 3. LẤY PROFILE KHÁC ---
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserProfile(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if(user == null) return ResponseEntity.notFound().build();

        // SỬA LỖI 2: Thêm <String, Object> trước .of
        return ResponseEntity.ok(Map.<String, Object>of(
                "id", user.getId(),
                "username", user.getUsername(),
                "fullName", user.getFullName() != null ? user.getFullName() : "",
                "phone", user.getPhone() != null ? user.getPhone() : "",
                "role", user.getRole() != null ? user.getRole() : "CUSTOMER",
                "restaurantId", user.getRestaurantId() != null ? user.getRestaurantId() : 0
        ));
    }

    // --- CÁC API DÀNH CHO ADMIN ---

    // 4. Xem danh sách tất cả người dùng
    @GetMapping("/admin/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        List<User> users = userRepository.findAll();

        // SỬA LỖI 3: Thêm <String, Object> trước .of
        List<Map<String, Object>> safeUsers = users.stream().map(u -> Map.<String, Object>of(
                "id", u.getId(),
                "username", u.getUsername(),
                "fullName", u.getFullName() != null ? u.getFullName() : "",
                "role", u.getRole() != null ? u.getRole() : "CUSTOMER",
                "restaurantId", u.getRestaurantId() != null ? u.getRestaurantId() : 0
        )).collect(Collectors.toList());

        return ResponseEntity.ok(safeUsers);
    }

    // 5. Xóa người dùng
    @DeleteMapping("/admin/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(id);
        return ResponseEntity.ok("Đã xóa người dùng thành công");
    }

    // 6. Sửa thông tin người dùng
    @PutMapping("/admin/users/{id}")
    public ResponseEntity<?> updateUserByAdmin(@PathVariable Long id, @RequestBody User req) {
        return userRepository.findById(id).map(user -> {
            user.setFullName(req.getFullName());
            user.setRole(req.getRole());
            user.setRestaurantId(req.getRestaurantId());
            if(req.getPhone() != null) user.setPhone(req.getPhone());

            userRepository.save(user);
            return ResponseEntity.ok("Cập nhật thông tin user thành công");
        }).orElse(ResponseEntity.notFound().build());
    }
}