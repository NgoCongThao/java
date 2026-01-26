package com.admin.backend.repository;

import com.admin.backend.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    @Query("""
        SELECT SUM(o.totalAmount)
        FROM Order o
        WHERE o.tenantId = :tenantId
        AND o.orderDate BETWEEN :from AND :to
    """)
    BigDecimal sumRevenue(Long tenantId, String from, String to);
}