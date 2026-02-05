package com.s2o.backend_api.repository;

import com.s2o.backend_api.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // ==========================================
    // 1. CHO KHÁCH HÀNG (CUSTOMER)
    // ==========================================

    // Lấy lịch sử đặt bàn của User (Kèm thông tin nhà hàng để hiển thị tên quán)
    @Query("SELECT b FROM Booking b JOIN FETCH b.restaurant WHERE b.user.id = :userId ORDER BY b.createdAt DESC")
    List<Booking> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);


    // ==========================================
    // 2. CHO STAFF & MANAGER (QUAN TRỌNG: MỚI THÊM)
    // ==========================================

    // Đây là hàm StaffController cần gọi để lấy danh sách bàn ĐÃ ĐẶT của riêng quán mình
    // Thầy dùng JOIN FETCH b.user để Staff nhìn thấy ngay tên khách hàng và SĐT
    @Query("SELECT b FROM Booking b JOIN FETCH b.user WHERE b.restaurant.id = :restaurantId ORDER BY b.createdAt DESC")
    List<Booking> findByRestaurantId(@Param("restaurantId") Long restaurantId);


    // ==========================================
    // 3. CHECK TRÙNG BÀN & TÌNH TRẠNG BÀN (NATIVE QUERY)
    // ==========================================

    // Đếm số lượng bàn đã đặt trong khung giờ đó
    @Query(value = "SELECT COUNT(*) FROM bookings " +
            "WHERE restaurant_id = :resId " +
            "AND booking_date = :date " +
            "AND booking_time >= CAST(:startTime AS TIME) " +
            "AND booking_time <= CAST(:endTime AS TIME) " +
            "AND status NOT IN ('CANCELLED', 'REJECTED')",
            nativeQuery = true)
    long countBookedTables(@Param("resId") Long resId,
                           @Param("date") LocalDate date,
                           @Param("startTime") LocalTime startTime,
                           @Param("endTime") LocalTime endTime);

    // Lấy danh sách các số bàn (table_number) đã bị đặt (để tô màu đỏ trên Frontend)
    @Query(value = "SELECT DISTINCT table_number FROM bookings " +
            "WHERE restaurant_id = :resId " +
            "AND booking_date = :date " +
            "AND booking_time >= CAST(:startTime AS TIME) " +
            "AND booking_time <= CAST(:endTime AS TIME) " +
            "AND status NOT IN ('CANCELLED', 'REJECTED') " +
            "AND table_number IS NOT NULL",
            nativeQuery = true)
    List<Integer> findBookedTableNumbers(@Param("resId") Long resId,
                                         @Param("date") LocalDate date,
                                         @Param("startTime") LocalTime startTime,
                                         @Param("endTime") LocalTime endTime);


    // ==========================================
    // 4. CHO BẾP (KITCHEN)
    // ==========================================

    // Lấy booking có đặt trước món ăn (để bếp chuẩn bị)
    // Chỉ lấy những đơn chưa hoàn thành (khác excludedStatus)
    @Query("SELECT b FROM Booking b " +
            "LEFT JOIN FETCH b.items " +
            "JOIN b.restaurant r " +
            "WHERE r.id = :restaurantId " +
            "AND b.status != :excludedStatus " +
            "AND SIZE(b.items) > 0 " +
            "ORDER BY b.createdAt ASC")
    List<Booking> findKitchenBookings(@Param("restaurantId") Long restaurantId,
                                      @Param("excludedStatus") String excludedStatus);
}