package com.admin.backend.controller;

import com.admin.backend.entity.User;
import com.admin.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Danh sách nhân viên
    @GetMapping
    public List<User> getUsers(HttpServletRequest req) {
        Long tenantId = (Long) req.getAttribute("tenantId");
        return userService.getAllByTenant(tenantId);
    }

    // Tạo nhân viên mới
  @PostMapping
public ResponseEntity<?> create(@RequestBody User user, HttpServletRequest req) {
    try {
        // TẠM THỜI: Gán cứng là 1L vì Filter đang tắt
        // Long tenantId = (Long) req.getAttribute("tenantId"); 
        Long tenantId = 1L; 

        System.out.println("DEBUG: Creating user with username: " + user.getUsername());
        System.out.println("DEBUG: Full Name received: " + user.getFullName()); // Kiểm tra xem JsonProperty chạy chưa
        
        User createdUser = userService.create(user, tenantId);
        
        return ResponseEntity.ok("Thêm nhân viên thành công: " + createdUser.getUsername()); 
    } catch (Exception e) {
        e.printStackTrace(); 
        return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
    }
}

    // Đổi role nhân viên
    @PutMapping("/{id}/role")
    public User updateRole(
            @PathVariable Integer id,
            @RequestParam User.Role role,
            HttpServletRequest req
    ) {
        Long tenantId = (Long) req.getAttribute("tenantId");
        return userService.updateRole(id, role, tenantId);
    }
}