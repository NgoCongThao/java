package com.example.backend.repository;

import com.example.backend.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("""
        SELECT SUM(o.quantity * o.menu.price)
        FROM OrderEntity o
    """)
    Double sumRevenue();
}
