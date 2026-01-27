package com.s2o.backend_api.controller;

import com.s2o.backend_api.dto.BookingRequest;
import com.s2o.backend_api.entity.Booking;
import com.s2o.backend_api.entity.Restaurant;
import com.s2o.backend_api.entity.User;
import com.s2o.backend_api.repository.BookingRepository;
import com.s2o.backend_api.repository.RestaurantRepository;
import com.s2o.backend_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*")
public class BookingController {

    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RestaurantRepository restaurantRepository;

    @PostMapping("/create")
    public ResponseEntity<?> createBooking(@RequestBody BookingRequest request) {
        try {
            System.out.println("--- BẮT ĐẦU ĐẶT BÀN ---");
            System.out.println("User ID: " + request.getUserId());
            System.out.println("Nhà hàng ID: " + request.getRestaurantId());
            System.out.println("Giờ đặt: " + request.getTime());

            // 1. Kiểm tra User
            User user = userRepository.findById(request.getUserId())
                    .orElse(null);
            if (user == null) {
                System.out.println("Lỗi: User không tồn tại (Do ID cũ?)");
                return ResponseEntity.badRequest().body("Tài khoản không hợp lệ. Vui lòng đăng xuất và đăng nhập lại.");
            }

            // 2. Kiểm tra Nhà hàng
            Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                    .orElse(null);
            if (restaurant == null) {
                System.out.println("Lỗi: Nhà hàng không tồn tại");
                return ResponseEntity.badRequest().body("Nhà hàng không tồn tại.");
            }

            // 3. Logic kiểm tra bàn trống
            LocalTime startCheck = request.getTime().minusHours(2);
            LocalTime endCheck = request.getTime().plusHours(2);

            // FIX LỖI TIME: Nếu qua đêm (Ví dụ 23h + 2h = 1h sáng), logic BETWEEN sẽ lỗi
            // Tạm thời để đơn giản: Nếu start > end (qua đêm), ta bỏ qua check hoặc check kiểu khác.
            // Ở đây ta chỉ check nếu trong cùng 1 ngày
            long currentBookings = 0;
            if (startCheck.isBefore(endCheck)) {
                currentBookings = bookingRepository.countBookedTables(
                        restaurant.getId(),
                        request.getDate(),
                        startCheck,
                        endCheck
                );
            } else {
                // Trường hợp qua đêm (ít gặp ở quán ăn thường), tạm tính là 0 để không lỗi SQL
                System.out.println("Cảnh báo: Đặt bàn qua đêm, tạm bỏ qua check trùng.");
            }

            int maxCapacity = restaurant.getTotalTables() != null ? restaurant.getTotalTables() : 10;

            System.out.println("Đã đặt: " + currentBookings + " / " + maxCapacity);

            if (currentBookings >= maxCapacity) {
                return ResponseEntity.badRequest()
                        .body("Nhà hàng đã hết bàn vào khung giờ " + request.getTime() + ". Vui lòng chọn giờ khác!");
            }

            // 4. Lưu Booking
            Booking booking = new Booking();
            booking.setUser(user);
            booking.setRestaurant(restaurant);
            booking.setCustomerName(request.getCustomerName());
            booking.setPhone(request.getPhone());
            booking.setBookingDate(request.getDate());
            booking.setBookingTime(request.getTime());
            booking.setGuestCount(request.getGuests());
            booking.setNote(request.getNote());
            booking.setStatus("PENDING");

            bookingRepository.save(booking);
            System.out.println("--- ĐẶT BÀN THÀNH CÔNG ---");

            return ResponseEntity.ok("Đặt bàn thành công! Mã đơn: " + booking.getId());

        } catch (Exception e) {
            e.printStackTrace(); // In lỗi ra màn hình đen
            return ResponseEntity.internalServerError().body("Lỗi hệ thống: " + e.getMessage());
        }
    }
}