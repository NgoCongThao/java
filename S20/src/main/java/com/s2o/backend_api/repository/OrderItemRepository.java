package com.s2o.backend_api.repository;

import com.s2o.backend_api.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    // Hiện tại chưa cần hàm custom nào, để trống là được
}