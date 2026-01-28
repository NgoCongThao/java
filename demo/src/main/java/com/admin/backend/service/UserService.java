package com.admin.backend.service;
import java.util.Optional;
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
    System.out.println("--- DEBUG CREATE USER ---");
    System.out.println("Username: " + user.getUsername());
    System.out.println("Full Name nhận được: " + user.getFullName());
    System.out.println("TenantId nhận được: " + tenantId);
    
    if (tenantId == null) {
        throw new RuntimeException("Lỗi: Không tìm thấy Tenant ID từ Token!");
    }
    
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
    // Thêm vào trong class UserService

public void deleteUser(Integer userId, Long tenantId) {
    // 1. Tìm user
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + userId));

    // 2. Kiểm tra Tenant (Bảo mật)
    if (!user.getTenantId().equals(tenantId)) {
        throw new RuntimeException("Lỗi bảo mật: Bạn không được xóa nhân viên của nhà hàng khác!");
    }

    // 3. Xóa
    userRepository.delete(user);
}
}