package com.s2o.backend_api.repository;

import com.s2o.backend_api.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // Tìm đơn hàng theo User ID (Sắp xếp đơn mới nhất lên đầu)
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Hoặc dùng tên đơn giản này (để khớp với code Controller cũ của bạn)
    List<Order> findByUserId(Long userId);
    // --- BỔ SUNG DÒNG NÀY ĐỂ HẾT LỖI ĐỎ ---
    // Spring Boot sẽ tự dịch thành: SELECT COUNT(*) FROM orders WHERE user_id = ?
    long countByUserId(Long userId);
    // ... các method hiện có

// THÊM METHOD CHO BẾP
    List<Order> findByRestaurantIdAndStatusNotOrderByCreatedAtAsc(Long restaurantId, String status);
}