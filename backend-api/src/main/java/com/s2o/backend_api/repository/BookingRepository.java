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

    // --- QUAN TRỌNG: QUERY NÀY SẼ LẤY LUÔN THÔNG TIN NHÀ HÀNG ---
    @Query("SELECT b FROM Booking b JOIN FETCH b.restaurant WHERE b.user.id = :userId ORDER BY b.createdAt DESC")
    List<Booking> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
    // ------------------------------------------------------------

    // Đếm số lượng bàn đã đặt (Logic kiểm tra full bàn)
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
}