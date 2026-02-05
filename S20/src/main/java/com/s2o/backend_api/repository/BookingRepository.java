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

    // 1. Lấy lịch sử của User
    @Query("SELECT b FROM Booking b JOIN FETCH b.restaurant WHERE b.user.id = :userId ORDER BY b.createdAt DESC")
    List<Booking> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    // 2. Query cũ (Chỉ giữ lại để tham khảo, logic chính sẽ dùng hàm findBookingsForConflictCheck bên dưới)
    @Query(value = "SELECT DISTINCT table_number FROM bookings " +
                   "WHERE restaurant_id = :resId " +
                   "AND booking_date = :date " +
                   "AND status NOT IN ('CANCELLED', 'REJECTED') " +
                   "AND table_number IS NOT NULL", 
           nativeQuery = true)
    List<Integer> findBookedTableNumbers(@Param("resId") Long resId,
                                         @Param("date") LocalDate date);

    // 3. Method cho Bếp
    @Query("SELECT b FROM Booking b " +
           "LEFT JOIN FETCH b.items " +
           "JOIN b.restaurant r " +
           "WHERE r.id = :restaurantId " +
           "AND b.status != :excludedStatus " +
           "AND SIZE(b.items) > 0 " +
           "ORDER BY b.createdAt ASC")
    List<Booking> findKitchenBookings(@Param("restaurantId") Long restaurantId,
                                      @Param("excludedStatus") String excludedStatus);

    // --- [HÀM QUAN TRỌNG NHẤT: JAVA LOGIC DATA SOURCE] ---
    // Lấy danh sách booking để Java tự xử lý logic trùng giờ.
    // Cập nhật: Thêm điều kiện (:tableNumber IS NULL OR ...) để hỗ trợ lấy tất cả bàn
    @Query("SELECT b FROM Booking b " +
           "WHERE b.restaurant.id = :resId " +
           "AND (:tableNumber IS NULL OR b.tableNumber = :tableNumber) " +
           "AND b.bookingDate = :date " +
           "AND b.status NOT IN ('CANCELLED', 'REJECTED')")
    List<Booking> findBookingsForConflictCheck(@Param("resId") Long resId,
                                               @Param("tableNumber") Integer tableNumber,
                                               @Param("date") LocalDate date);
    
    // Hàm đếm tổng (check full quán)
    @Query("SELECT COUNT(b) FROM Booking b " +
           "WHERE b.restaurant.id = :resId " +
           "AND b.bookingDate = :date " +
           "AND b.status NOT IN ('CANCELLED', 'REJECTED')")
    long countTotalBookingsInDay(@Param("resId") Long resId, 
                                 @Param("date") LocalDate date);
}