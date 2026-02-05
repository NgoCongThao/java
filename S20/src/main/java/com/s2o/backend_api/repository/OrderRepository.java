package com.s2o.backend_api.repository;

import com.s2o.backend_api.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
@Query(value = "SELECT " +    // Tuấn thêm đoạn code này để tính doanh thu theo khoảng thời gian
       "(SELECT COALESCE(SUM(total_price), 0) FROM orders " +
       " WHERE restaurant_id = :resId AND status = 'COMPLETED' " +
       " AND created_at >= :start AND created_at <= :end) " + // Dùng >= và <= thay cho BETWEEN
       "+ " +
       "(SELECT COALESCE(SUM(total_price), 0) FROM bookings " +
       " WHERE restaurant_id = :resId AND status = 'COMPLETED' " +
       " AND booking_date >= CAST(:start AS DATE) AND booking_date <= CAST(:end AS DATE))", 
       nativeQuery = true)
Double calculateRevenueByRange(
    @Param("resId") Long resId, 
    @Param("start") String start, // Chuyển sang String
    @Param("end") String end      // Chuyển sang String
);

@Query(value = "SELECT " +   // // Tuấn thêm đoạn code này để tính tổng doanh thu
       "(SELECT COALESCE(SUM(total_price), 0) FROM orders WHERE restaurant_id = :resId AND status = 'COMPLETED') + " +
       "(SELECT COALESCE(SUM(total_price), 0) FROM bookings WHERE restaurant_id = :resId AND status = 'COMPLETED')", 
       nativeQuery = true)
Double calculateGrandTotalRevenue(@Param("resId") Long resId);
}
