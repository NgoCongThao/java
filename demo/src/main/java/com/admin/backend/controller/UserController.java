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
        // Lấy TenantId từ Token (do JwtFilter đã giải mã và đặt vào đây)
        Long tenantId = (Long) req.getAttribute("tenantId");
        
        // Kiểm tra an toàn: Nếu không có Token hoặc Token sai, cái này sẽ null
        if (tenantId == null) {
            return ResponseEntity.status(401).body("Lỗi: Không tìm thấy Tenant ID. Bạn đã đăng nhập chưa?");
        }

        System.out.println("DEBUG: Tạo user cho Tenant ID: " + tenantId);

        // Gọi Service lưu vào DB
        User createdUser = userService.create(user, tenantId);
        return ResponseEntity.ok("Thêm thành công user: " + createdUser.getUsername());
        
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(500).body("Lỗi hệ thống: " + e.getMessage());
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

    // Thêm vào trong class UserController

@DeleteMapping("/{id}")
public ResponseEntity<?> deleteUser(@PathVariable Integer id, HttpServletRequest req) {
    try {
        Long tenantId = (Long) req.getAttribute("tenantId");
        
        // Gọi Service xử lý
        userService.deleteUser(id, tenantId);
        
        return ResponseEntity.ok("Đã xóa nhân viên thành công!");
    } catch (RuntimeException e) {
        // Lỗi do không tìm thấy hoặc không đúng Tenant
        return ResponseEntity.badRequest().body(e.getMessage());
    } catch (Exception e) {
        // Lỗi DB (Ví dụ: Nhân viên đã có dữ liệu hóa đơn)
        return ResponseEntity.status(500).body("Không thể xóa: " + e.getMessage());
    }
}
@PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Integer id, @RequestBody User userRequest, HttpServletRequest req) {
        try {
            Long tenantId = (Long) req.getAttribute("tenantId");
            if (tenantId == null) {
                 return ResponseEntity.status(401).body("Lỗi: Token không hợp lệ.");
            }

            User updatedUser = userService.updateUser(id, userRequest, tenantId);
            
            return ResponseEntity.ok("Cập nhật thành công nhân viên: " + updatedUser.getUsername());

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi hệ thống: " + e.getMessage());
        }
    }
}