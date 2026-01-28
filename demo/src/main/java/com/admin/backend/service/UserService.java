package com.admin.backend.service;

import com.admin.backend.entity.User;
import com.admin.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // 1. Khai báo công cụ mã hóa

    // 2. Constructor nhận cả Repository và PasswordEncoder
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // --- CÁC HÀM CŨ ---

    public List<User> getAllByTenant(Long tenantId) {
        return userRepository.findAll()
                .stream()
                .filter(u -> tenantId.equals(u.getTenantId()))
                .toList();
    }

    public User create(User user, Long tenantId) {
        if (tenantId == null) {
            throw new RuntimeException("Lỗi: Không tìm thấy Tenant ID từ Token!");
        }
        user.setTenantId(tenantId);
        
        // Mã hóa mật khẩu khi tạo mới
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        return userRepository.save(user);
    }

    public User updateRole(Integer id, User.Role role, Long tenantId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        if (!user.getTenantId().equals(tenantId)) {
            throw new RuntimeException("Không có quyền sửa user này");
        }
        user.setRole(role);
        return userRepository.save(user);
    }

    public void deleteUser(Integer userId, Long tenantId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + userId));

        if (!user.getTenantId().equals(tenantId)) {
            throw new RuntimeException("Lỗi bảo mật: Bạn không được xóa nhân viên của nhà hàng khác!");
        }
        userRepository.delete(user);
    }

    // --- HÀM MỚI (ĐỂ SỬA LỖI CỦA BẠN) ---

    public User updateUser(Integer id, User request, Long tenantId) {
        // 1. Tìm user cũ
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại!"));

        // 2. Check bảo mật Tenant
        if (!user.getTenantId().equals(tenantId)) {
            throw new RuntimeException("Bạn không được sửa nhân viên của chi nhánh khác!");
        }

        // 3. Cập nhật Tên (nếu có gửi lên)
        if (request.getFullName() != null && !request.getFullName().isEmpty()) {
            user.setFullName(request.getFullName());
        }

        // 4. Cập nhật Username (nếu có gửi lên)
        if (request.getUsername() != null && !request.getUsername().isEmpty()) {
            user.setUsername(request.getUsername());
        }

        // 5. Cập nhật Role (nếu có gửi lên)
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        // 6. Đổi mật khẩu (QUAN TRỌNG: Mã hóa trước khi lưu)
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            String encodedPassword = passwordEncoder.encode(request.getPassword());
            user.setPassword(encodedPassword);
        }

        return userRepository.save(user);
    }
}