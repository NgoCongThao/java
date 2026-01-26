package com.admin.backend.controller;

import com.admin.backend.entity.User;
import com.admin.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
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
    public User create(@RequestBody User user, HttpServletRequest req) {
        Long tenantId = (Long) req.getAttribute("tenantId");
        return userService.create(user, tenantId);
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