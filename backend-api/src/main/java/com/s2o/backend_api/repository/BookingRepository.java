package com.s2o.backend_api.repository;

import com.s2o.backend_api.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List; // <--- Đừng quên import List

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // --- 1. THÊM HÀM NÀY ĐỂ LẤY LỊCH SỬ ĐẶT BÀN ---
    // Spring Data JPA sẽ tự động hiểu: Tìm theo UserId và Sắp xếp ngày tạo giảm dần (mới nhất lên đầu)
    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);


    // --- 2. HÀM CŨ CHECK TRÙNG BÀN (GIỮ NGUYÊN) ---
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
}