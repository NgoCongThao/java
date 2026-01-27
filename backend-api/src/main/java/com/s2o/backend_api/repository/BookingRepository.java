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

    // Lấy lịch sử đặt bàn của user
    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Đếm số bàn đã đặt (hàm cũ)
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

    // THÊM PHƯƠNG THỨC MỚI: Lấy danh sách số bàn đã được đặt trong khung giờ
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