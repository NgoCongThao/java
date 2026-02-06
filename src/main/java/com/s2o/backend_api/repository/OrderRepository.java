package com.s2o.backend_api.repository;

import com.s2o.backend_api.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // ==========================================
    // 1. NHÓM HÀM CHO KHÁCH HÀNG (CUSTOMER)
    // ==========================================

    // Tìm các đơn đang phục vụ (chưa thanh toán) để check bàn trống
    @Query("SELECT o FROM Order o WHERE o.status NOT IN ('PAID', 'CANCELLED') AND o.tableNumber IS NOT NULL")
    List<Order> findActiveOrders();

    // Tìm lịch sử đơn hàng của user
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Order> findByUserId(Long userId);
    long countByUserId(Long userId);

    // Tìm đơn đang hoạt động theo Bàn (Cũ - Có thể giữ lại hoặc bỏ)
    Optional<Order> findFirstByTableNumberAndStatusIn(int tableNumber, List<String> statuses);

    // 🔥🔥🔥 [QUAN TRỌNG - THÊM HÀM NÀY] 🔥🔥🔥
    // Hàm này giúp Controller tìm chính xác đơn cũ của Nhà hàng X tại Bàn Y
    // Để thực hiện logic GỘP ĐƠN khi quét QR
    Optional<Order> findFirstByRestaurantIdAndTableNumberAndStatusIn(
            Long restaurantId,
            int tableNumber,
            List<String> statuses
    );

    @Query("SELECT DISTINCT o.tableNumber FROM Order o " +
            "WHERE o.restaurantId = :resId " +
            "AND o.tableNumber > 0 " +
            "AND o.status IN ('PENDING', 'COOKING', 'DELIVERING', 'READY', 'PAYMENT_REQUEST')")
    List<Integer> findBusyTableNumbers(@Param("resId") Long resId);
    // ==========================================
    // 2. NHÓM HÀM CHO QUẢN LÝ (MANAGER) & NHÂN VIÊN (STAFF)
    // ==========================================

    List<Order> findByRestaurantIdOrderByCreatedAtDesc(Long restaurantId);

    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE o.restaurantId = :restaurantId AND o.status = 'COMPLETED'")
    Double calculateRevenueByRestaurant(@Param("restaurantId") Long restaurantId);


    // ==========================================
    // 3. NHÓM HÀM CHO ADMIN HỆ THỐNG
    // ==========================================

    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE o.status = 'COMPLETED'")
    Double calculateTotalSystemRevenue();


    // ==========================================
    // 4. NHÓM HÀM CHO BẾP (KITCHEN)
    // ==========================================

    List<Order> findByRestaurantIdAndStatusNotOrderByCreatedAtAsc(Long restaurantId, String status);
    List<Order> findByRestaurantId(Long restaurantId);
    List<Order> findByRestaurantIdAndStatus(Long resId, String status);
}