package com.s2o.backend_api.repository;

import com.s2o.backend_api.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // SỬA LẠI: Ép kiểu tham số truyền vào thành TIME để SQL Server hiểu
    @Query(value = "SELECT COUNT(*) FROM bookings " +
                   "WHERE restaurant_id = :resId " +
                   "AND booking_date = :date " +
                   "AND booking_time >= CAST(:startTime AS TIME) " +  // <--- Thêm CAST
                   "AND booking_time <= CAST(:endTime AS TIME) " +    // <--- Thêm CAST
                   "AND status NOT IN ('CANCELLED', 'REJECTED')", 
           nativeQuery = true)
    long countBookedTables(@Param("resId") Long resId, 
                           @Param("date") LocalDate date, 
                           @Param("startTime") LocalTime startTime, 
                           @Param("endTime") LocalTime endTime);
}