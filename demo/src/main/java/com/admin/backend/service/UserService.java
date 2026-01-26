package com.admin.backend.service;

import com.admin.backend.entity.User;
import com.admin.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Lấy danh sách nhân viên theo tenant
    public List<User> getAllByTenant(Long tenantId) {
        return userRepository.findAll()
                .stream()
                .filter(u -> tenantId.equals(u.getTenantId()))
                .toList();
    }

    // Tạo nhân viên mới
    public User create(User user, Long tenantId) {
        user.setTenantId(tenantId);
        return userRepository.save(user);
    }

    // Cập nhật role nhân viên
    public User updateRole(Integer id, User.Role role, Long tenantId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        if (!user.getTenantId().equals(tenantId)) {
            throw new RuntimeException("Không có quyền sửa user này");
        }

        user.setRole(role);
        return userRepository.save(user);
    }
}