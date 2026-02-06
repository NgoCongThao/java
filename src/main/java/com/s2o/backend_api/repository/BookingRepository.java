package com.s2o.backend_api.repository;

import com.s2o.backend_api.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // ==========================================
    // 1. CHO KHÁCH HÀNG (CUSTOMER)
    // ==========================================

    @Query("SELECT b FROM Booking b JOIN FETCH b.restaurant WHERE b.user.id = :userId ORDER BY b.createdAt DESC")
    List<Booking> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);


    // ==========================================
    // 2. CHO STAFF & MANAGER
    // ==========================================

    @Query("SELECT b FROM Booking b JOIN FETCH b.user WHERE b.restaurant.id = :restaurantId ORDER BY b.createdAt DESC")
    List<Booking> findByRestaurantId(@Param("restaurantId") Long restaurantId);


    // ==========================================
    // 3. CHECK TRÙNG BÀN & TÌNH TRẠNG BÀN (NATIVE QUERY)
    // ==========================================

    // 🔥 [ĐÃ SỬA] Đếm số lượng bàn bận (Loại bỏ đơn đã COMPLETED)
    @Query(value = "SELECT COUNT(*) FROM bookings " +
            "WHERE restaurant_id = :resId " +
            "AND booking_date = :date " +
            "AND booking_time >= CAST(:startTime AS TIME) " +
            "AND booking_time <= CAST(:endTime AS TIME) " +
            "AND status NOT IN ('CANCELLED', 'REJECTED', 'COMPLETED')", // <--- THÊM 'COMPLETED' VÀO ĐÂY
            nativeQuery = true)
    long countBookedTables(@Param("resId") Long resId,
                           @Param("date") LocalDate date,
                           @Param("startTime") LocalTime startTime,
                           @Param("endTime") LocalTime endTime);

    // 🔥 [ĐÃ SỬA] Lấy danh sách số bàn đang bận (Loại bỏ đơn đã COMPLETED)
    @Query(value = "SELECT DISTINCT table_number FROM bookings " +
            "WHERE restaurant_id = :resId " +
            "AND booking_date = :date " +
            "AND booking_time >= CAST(:startTime AS TIME) " +
            "AND booking_time <= CAST(:endTime AS TIME) " +
            "AND status NOT IN ('CANCELLED', 'REJECTED', 'COMPLETED') " + // <--- THÊM 'COMPLETED' VÀO ĐÂY
            "AND table_number IS NOT NULL",
            nativeQuery = true)
    List<Integer> findBookedTableNumbers(@Param("resId") Long resId,
                                         @Param("date") LocalDate date,
                                         @Param("startTime") LocalTime startTime,
                                         @Param("endTime") LocalTime endTime);


    // ==========================================
    // 4. CHO BẾP (KITCHEN)
    // ==========================================

    @Query("SELECT b FROM Booking b " +
            "LEFT JOIN FETCH b.items " +
            "JOIN b.restaurant r " +
            "WHERE r.id = :restaurantId " +
            "AND b.status != :excludedStatus " +
            "AND SIZE(b.items) > 0 " +
            "ORDER BY b.createdAt ASC")
    List<Booking> findKitchenBookings(@Param("restaurantId") Long restaurantId,
                                      @Param("excludedStatus") String excludedStatus);

    @Query("SELECT b FROM Booking b WHERE b.bookingDate = :date AND b.status = 'CONFIRMED'")
    List<Booking> findConfirmedBookingsByDate(@Param("date") LocalDate date);
    @Query("SELECT b FROM Booking b WHERE b.restaurant.id = :resId " +
            "AND b.tableNumber = :tableNum " +
            "AND b.bookingDate = :date " +
            "AND b.status NOT IN ('CANCELLED', 'REJECTED', 'COMPLETED')")
    Optional<Booking> findActiveBookingAtTable(@Param("resId") Long resId,
                                               @Param("tableNum") Integer tableNum,
                                               @Param("date") LocalDate date);
}