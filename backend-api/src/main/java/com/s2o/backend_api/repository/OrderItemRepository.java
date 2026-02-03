package com.s2o.backend_api.repository;

import com.s2o.backend_api.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // --- QUERY CHO BẾP ---
    // Lấy tất cả các món ăn có trạng thái 'PENDING' (Chờ nấu) thuộc về nhà hàng này
    // Sắp xếp theo thời gian đơn hàng (Order) tạo ra => Ai đến trước nấu trước
    @Query("SELECT oi FROM OrderItem oi JOIN oi.order o " +
            "WHERE o.restaurantId = :restaurantId " +
            "AND oi.status = 'PENDING' " +
            "ORDER BY o.createdAt ASC")
    List<OrderItem> findPendingItemsByRestaurant(@Param("restaurantId") Long restaurantId);

    // THỐNG KÊ: Top món ăn bán chạy nhất của quán
    // Trả về List<Object[]>: [Tên món, Tổng số lượng]
    @Query("SELECT oi.itemName, SUM(oi.quantity) FROM OrderItem oi " +
            "JOIN oi.order o " +
            "WHERE o.restaurantId = :restaurantId AND o.status = 'COMPLETED' " +
            "GROUP BY oi.itemName " +
            "ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findTopSellingItems(@Param("restaurantId") Long restaurantId);
}
