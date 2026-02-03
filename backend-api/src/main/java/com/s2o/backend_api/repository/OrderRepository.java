package com.s2o.backend_api.repository;

import com.s2o.backend_api.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // ==========================================
    // 1. NHÓM HÀM CHO KHÁCH HÀNG (CUSTOMER)
    // ==========================================

    // Tìm đơn hàng theo User ID (Sắp xếp đơn mới nhất lên đầu để khách dễ xem lịch sử)
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Hàm viết tắt (để tương thích code cũ nếu có)
    List<Order> findByUserId(Long userId);

    // Đếm số đơn hàng của khách (Có thể dùng để tính hạng thành viên: Bạc, Vàng...)
    long countByUserId(Long userId);


    // ==========================================
    // 2. NHÓM HÀM CHO QUẢN LÝ (MANAGER) & NHÂN VIÊN (STAFF)
    // ==========================================

    // Lấy TOÀN BỘ đơn hàng của quán mình (Sắp xếp mới nhất trước)
    // Dùng cho: Màn hình quản lý đơn hàng của Staff/Manager
    List<Order> findByRestaurantIdOrderByCreatedAtDesc(Long restaurantId);

    // TÍNH DOANH THU RIÊNG CỦA QUÁN
    // Logic: Chỉ cộng tổng tiền (totalPrice) các đơn đã hoàn thành (COMPLETED) của quán đó
    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE o.restaurantId = :restaurantId AND o.status = 'COMPLETED'")
    Double calculateRevenueByRestaurant(@Param("restaurantId") Long restaurantId);


    // ==========================================
    // 3. NHÓM HÀM CHO ADMIN HỆ THỐNG (SYSTEM ADMIN)
    // ==========================================

    // TÍNH DOANH THU TOÀN SÀN (TỔNG TẤT CẢ CÁC QUÁN)
    // Dùng cho: Màn hình Dashboard của Admin hệ thống
    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE o.status = 'COMPLETED'")
    Double calculateTotalSystemRevenue();


    // ==========================================
    // 4. NHÓM HÀM CHO BẾP (KITCHEN)
    // ==========================================

    // Lấy danh sách đơn CẦN XỬ LÝ (Không lấy đơn đã xong hoặc hủy)
    // Param status thường truyền vào là "COMPLETED" (nghĩa là lấy tất cả status KHÁC Completed)
    // Sắp xếp cũ nhất lên đầu (Asc) để bếp làm theo thứ tự ai đến trước phục vụ trước
    List<Order> findByRestaurantIdAndStatusNotOrderByCreatedAtAsc(Long restaurantId, String status);
    List<Order> findByRestaurantId(Long restaurantId);
}
