package com.s2o.backend_api.repository;

import com.s2o.backend_api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List; // <-- Quan trọng: Phải import List
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Tìm user theo username
    Optional<User> findByUsername(String username);
    
    // Kiểm tra tồn tại
    Boolean existsByUsername(String username);

    // --- 👇 QUAN TRỌNG: THÊM HÀM NÀY ĐỂ SỬA LỖI CHO PARTNER CONTROLLER 👇 ---
    // Tìm danh sách nhân viên theo: ID Quán + Vai trò (KITCHEN) + Trạng thái (PENDING/ACTIVE)
    List<User> findByRestaurantIdAndRoleAndStatus(Long restaurantId, String role, String status);
}