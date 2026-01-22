package com.s2o.backend.repository;

import com.s2o.backend.entity.Order;
import com.s2o.backend.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional; // Nhớ import Optional để tránh lỗi null

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // 1. Tìm tất cả đơn hàng của một nhà hàng (Dùng cho trang Danh sách đơn)
    List<Order> findByRestaurantId(Long restaurantId);

    // 2. Tìm đơn hàng "đang hoạt động" tại một bàn cụ thể (Dùng cho logic Gộp Đơn)
    // Dịch ra tiếng Việt: Tìm đơn ở bàn X VÀ trạng thái KHÔNG PHẢI là A VÀ trạng thái KHÔNG PHẢI là B
    Optional<Order> findByTableIdAndStatusNotAndStatusNot(Long tableId, OrderStatus status1, OrderStatus status2);
}