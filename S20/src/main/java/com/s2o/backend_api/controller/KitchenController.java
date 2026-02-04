package com.s2o.backend_api.controller;

import com.s2o.backend_api.entity.Booking;
import com.s2o.backend_api.entity.Order;
import com.s2o.backend_api.entity.OrderItem;
import com.s2o.backend_api.entity.User;
import com.s2o.backend_api.repository.BookingRepository;
import com.s2o.backend_api.repository.OrderRepository;
import com.s2o.backend_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/kitchen")
@CrossOrigin(origins = "*")
public class KitchenController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    // === ENDPOINT MỚI: LẤY GỘP ORDERS + BOOKINGS CÓ MÓN ĂN KÈM ===
    @GetMapping("/all")
    public ResponseEntity<List<Map<String, Object>>> getAllKitchenItems(@RequestParam Long restaurantId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null) {
            return ResponseEntity.status(401).build();
        }

        Optional<User> currentUserOpt = userRepository.findByUsername(auth.getName());
        if (currentUserOpt.isEmpty() || currentUserOpt.get().getRestaurantId() == null
                || !currentUserOpt.get().getRestaurantId().equals(restaurantId)) {
            return ResponseEntity.status(403).build();
        }

        // 1. Lấy orders chưa hoàn tất
        List<Order> orders = orderRepository
                .findByRestaurantIdAndStatusNotOrderByCreatedAtAsc(restaurantId, "COMPLETED");

        // 2. Lấy bookings có món ăn kèm, chưa hoàn tất
        List<Booking> bookings = bookingRepository
                .findKitchenBookings(restaurantId, "COMPLETED");

        // 3. Gộp vào một list Map để dễ serialize JSON và frontend phân biệt
        List<Map<String, Object>> allItems = new ArrayList<>();

        // Thêm orders
        for (Order order : orders) {
            Map<String,  Object> item = new HashMap<>();
            item.put("type", "order");
            item.put("id", order.getId());
            item.put("status", order.getStatus());
            item.put("createdAt", order.getCreatedAt());
            item.put("totalPrice", order.getTotalPrice());
            item.put("tableNumber", order.getTableNumber());
            item.put("note", order.getNote());
            item.put("items", order.getItems());
            item.put("customerInfo", null);
            allItems.add(item);
        }

        // Thêm bookings
        for (Booking booking : bookings) {
            Map<String, Object> item = new HashMap<>();
            item.put("type", "booking");
            item.put("id", booking.getId());
            item.put("status", booking.getStatus());
            item.put("createdAt", booking.getCreatedAt());

            // Tính totalPrice từ items (vì Booking không có field totalPrice)
            Double total = 0.0;
            if (booking.getItems() != null) {
                total = booking.getItems().stream()
                        .mapToDouble(i -> i.getPrice() * i.getQuantity())
                        .sum();
            }
            item.put("totalPrice", total);

            item.put("tableNumber", booking.getTableNumber());
            item.put("note", booking.getNote());
            item.put("items", booking.getItems());

            // Thông tin khách hàng đặt bàn
            item.put("customerInfo", booking.getCustomerName() + " - " + booking.getPhone());

            allItems.add(item);
        }

        // Sắp xếp chung theo createdAt (cũ nhất lên đầu)
        allItems.sort((a, b) -> {
            Object dateA = a.get("createdAt");
            Object dateB = b.get("createdAt");
            if (dateA instanceof Comparable && dateB instanceof Comparable) {
                return ((Comparable) dateA).compareTo(dateB);
            }
            return 0;
        });

        return ResponseEntity.ok(allItems);
    }

    // === ENDPOINT CŨ: Chỉ lấy orders (giữ lại để tương thích nếu cần) ===
    @GetMapping("/orders")
    public ResponseEntity<List<Order>> getKitchenOrders(@RequestParam Long restaurantId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null) {
            return ResponseEntity.status(401).build();
        }

        Optional<User> currentUserOpt = userRepository.findByUsername(auth.getName());
        if (currentUserOpt.isEmpty() || currentUserOpt.get().getRestaurantId() == null
                || !currentUserOpt.get().getRestaurantId().equals(restaurantId)) {
            return ResponseEntity.status(403).build();
        }

        List<Order> orders = orderRepository
                .findByRestaurantIdAndStatusNotOrderByCreatedAtAsc(restaurantId, "COMPLETED");
        return ResponseEntity.ok(orders);
    }

    // === ENDPOINT CẬP NHẬT STATUS ORDER (giữ nguyên) ===
    @PutMapping("/orders/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id,
                                               @RequestBody Map<String, String> body) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        Optional<User> currentUserOpt = userRepository.findByUsername(auth.getName());
        Optional<Order> orderOpt = orderRepository.findById(id);

        if (orderOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Đơn hàng không tồn tại");
        }
        Order order = orderOpt.get();

        if (currentUserOpt.isEmpty() || currentUserOpt.get().getRestaurantId() == null
                || order.getRestaurantId() == null
                || !currentUserOpt.get().getRestaurantId().equals(order.getRestaurantId())) {
            return ResponseEntity.status(403).body("Không có quyền cập nhật đơn hàng này");
        }

        String newStatus = body.get("status");
        if (newStatus == null || newStatus.isEmpty()) {
            return ResponseEntity.badRequest().body("Trạng thái không hợp lệ");
        }

        order.setStatus(newStatus);
        orderRepository.save(order);

        Map<String, String> resp = new HashMap<>();
        resp.put("message", "Cập nhật trạng thái thành công");
        return ResponseEntity.ok(resp);
    }

    // === END  POINT MỚI: CẬP NHẬT STATUS BOOKING (SỬA LẠI ĐÚNG ĐƯỜNG DẪN VÀ CHECK RESTAURANT) ===
    @PutMapping("/bookings/{id}/status")
    public ResponseEntity<?> updateBookingStatus(@PathVariable Long id,
                                                 @RequestBody Map<String, String> body) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        Optional<User> currentUserOpt = userRepository.findByUsername(auth.getName());
        Optional<Booking> bookingOpt = bookingRepository.findById(id);

        if (bookingOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Đặt bàn không tồn tại");
        }
        Booking booking = bookingOpt.get();

        // Check restaurantId từ relationship restaurant (giả sử Booking có field Restaurant restaurant)
        Long bookingRestaurantId = booking.getRestaurant() != null ? booking.getRestaurant().getId() : null;

        if (currentUserOpt.isEmpty() || currentUserOpt.get().getRestaurantId() == null
                || bookingRestaurantId == null
                || !currentUserOpt.get().getRestaurantId().equals(bookingRestaurantId)) {
            return ResponseEntity.status(403).body("Không có quyền cập nhật đặt bàn này");
        }

        String newStatus = body.get("status");
        if (newStatus == null || newStatus.isEmpty()) {
            return ResponseEntity.badRequest().body("Trạng thái không hợp lệ");
        }

        booking.setStatus(newStatus);
        bookingRepository.save(booking);

        Map<String, String> resp = new HashMap<>();
        resp.put("message", "Cập nhật trạng thái thành công");
        return ResponseEntity.ok(resp);
    }
}