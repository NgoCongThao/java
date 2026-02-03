package com.s2o.backend_api.controller;

import com.s2o.backend_api.entity.Booking;
import com.s2o.backend_api.entity.Order;
import com.s2o.backend_api.entity.User;
import com.s2o.backend_api.repository.BookingRepository;
import com.s2o.backend_api.repository.OrderRepository;
import com.s2o.backend_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff")
public class StaffController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository; // <-- Cần cái này để tìm info nhân viên

    // --- HÀM HELPER: Lấy ID quán của nhân viên đang đăng nhập ---
    private Long getCurrentRestaurantId() {
        // Lấy username từ token đăng nhập
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        // Tìm nhân viên trong DB
        User staff = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên!"));
        // Trả về ID quán của nhân viên đó
        return staff.getRestaurantId();
    }

    // 1. Quản lý Booking (Đặt bàn)
    @GetMapping("/bookings")
    public List<Booking> getBookings() {
        // Gọi hàm helper để lấy ID
        Long currentRestaurantId = getCurrentRestaurantId();

        // Em cần thêm hàm findByRestaurantId trong BookingRepository tương tự như OrderRepository nhé
        return bookingRepository.findByRestaurantId(currentRestaurantId);

    }

    @PutMapping("/bookings/{id}/confirm")
    public ResponseEntity<?> confirmBooking(@PathVariable Long id) {
        Booking b = bookingRepository.findById(id).orElseThrow();
        b.setStatus("CONFIRMED");
        bookingRepository.save(b);
        return ResponseEntity.ok("Đã xác nhận bàn");
    }

    // 2. Quản lý Đơn hàng (Order)
    @GetMapping("/orders")
    public List<Order> getAllOrders() {
        // Gọi hàm helper để lấy ID quán
        Long currentRestaurantId = getCurrentRestaurantId();

        // Giờ thì biến currentRestaurantId đã có giá trị, hết báo đỏ nhé
        return orderRepository.findByRestaurantIdOrderByCreatedAtDesc(currentRestaurantId);
    }

    // 3. Thanh toán / Hoàn tất đơn
    @PutMapping("/orders/{id}/pay")
    public ResponseEntity<?> payOrder(@PathVariable Long id) {
        Order o = orderRepository.findById(id).orElseThrow();
        o.setStatus("COMPLETED");
        orderRepository.save(o);
        return ResponseEntity.ok("Thanh toán thành công");
    }
}