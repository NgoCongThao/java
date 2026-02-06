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

<<<<<<< HEAD
    // 2. Lấy danh sách booking của nhà hàng (để hiện thị admin/partner)
    List<Booking> findByRestaurantIdOrderByCreatedAtDesc(Long restaurantId);
=======
    // 2. Query cũ (Chỉ giữ lại để tham khảo, logic chính sẽ dùng hàm findBookingsForConflictCheck bên dưới)
    @Query(value = "SELECT DISTINCT table_number FROM bookings " +
                   "WHERE restaurant_id = :resId " +
                   "AND booking_date = :date " +
                   "AND status NOT IN ('CANCELLED', 'REJECTED') " +
                   "AND table_number IS NOT NULL", 
           nativeQuery = true)
    List<Integer> findBookedTableNumbers(@Param("resId") Long resId,
                                         @Param("date") LocalDate date);
>>>>>>> edfd887424fb24a79f0b1de399c73811c4707d16

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

<<<<<<< HEAD
    // 4. Query hiển thị bàn đỏ cũ (giữ lại để tham khảo, không dùng cho logic chính)
    @Query(value = "SELECT DISTINCT table_number FROM bookings " +
                   "WHERE restaurant_id = :resId " +
                   "AND booking_date = :date " +
                   "AND status NOT IN ('CANCELLED', 'REJECTED') " +
                   "AND table_number IS NOT NULL", 
           nativeQuery = true)
    List<Integer> findBookedTableNumbers(@Param("resId") Long resId, @Param("date") LocalDate date);

    // --- [HÀM QUAN TRỌNG NHẤT: JAVA LOGIC DATA SOURCE] ---
    // Lấy danh sách booking để Java tự xử lý logic trùng giờ.
    // Logic: (:tableNumber IS NULL) giúp lấy toàn bộ bàn khi cần vẽ sơ đồ
=======
    // --- [HÀM QUAN TRỌNG NHẤT: JAVA LOGIC DATA SOURCE] ---
    // Lấy danh sách booking để Java tự xử lý logic trùng giờ.
    // Cập nhật: Thêm điều kiện (:tableNumber IS NULL OR ...) để hỗ trợ lấy tất cả bàn
>>>>>>> edfd887424fb24a79f0b1de399c73811c4707d16
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
<<<<<<< HEAD
=======

List<Booking> findByRestaurantIdOrderByCreatedAtDesc(Long restaurantId);
>>>>>>> edfd887424fb24a79f0b1de399c73811c4707d16
}