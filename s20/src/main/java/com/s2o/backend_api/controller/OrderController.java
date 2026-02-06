package com.s2o.backend_api.controller;

import com.s2o.backend_api.dto.OrderRequest;
import com.s2o.backend_api.entity.Booking; // <--- Import Booking
import com.s2o.backend_api.entity.Order;
import com.s2o.backend_api.entity.OrderItem;
import com.s2o.backend_api.entity.User;
import com.s2o.backend_api.repository.BookingRepository; // <--- Import Repository
import com.s2o.backend_api.repository.OrderRepository;
import com.s2o.backend_api.repository.RestaurantRepository;
import com.s2o.backend_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository; // <--- QUAN TRỌNG: Thêm cái này để xử lý Booking

    // 1. API TẠO ĐƠN HÀNG (Dành cho trang Menu)
    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest req) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // =================================================================================
        // 🔥 LOGIC GỘP ĐƠN: KIỂM TRA BÀN + NHÀ HÀNG ĐANG ĂN 🔥
        // =================================================================================
        if (req.getTableNumber() != null && req.getTableNumber() > 0
                && !"DELIVERY".equalsIgnoreCase(req.getOrderType())
                && req.getRestaurantId() != null) {

            List<String> activeStatuses = Arrays.asList("PENDING", "COOKING", "DELIVERING", "READY");

            Optional<Order> existingOrderOpt = orderRepository.findFirstByRestaurantIdAndTableNumberAndStatusIn(
                    req.getRestaurantId(),
                    req.getTableNumber(),
                    activeStatuses
            );

            if (existingOrderOpt.isPresent()) {
                System.out.println("✅ GỘP ĐƠN: Tìm thấy đơn cũ ID: " + existingOrderOpt.get().getId());
                Order existingOrder = existingOrderOpt.get();

                if (req.getItems() != null) {
                    for (var i : req.getItems()) {
                        OrderItem item = new OrderItem();
                        item.setItemName(i.getName());
                        item.setQuantity(i.getQty());
                        item.setPrice(i.getPrice());
                        item.setOrder(existingOrder);
                        item.setStatus("PENDING");
                        existingOrder.getItems().add(item);
                    }
                }

                double additionalTotal = req.getTotal();
                existingOrder.setTotalPrice(existingOrder.getTotalPrice() + additionalTotal);
                existingOrder.setFinalPrice(existingOrder.getFinalPrice() + additionalTotal);

                if (req.getNote() != null && !req.getNote().isEmpty()) {
                    String oldNote = existingOrder.getNote() == null ? "" : existingOrder.getNote();
                    existingOrder.setNote(oldNote + " | Gọi thêm: " + req.getNote());
                }

                if ("READY".equals(existingOrder.getStatus())) {
                    existingOrder.setStatus("PENDING");
                }

                Order savedOrder = orderRepository.save(existingOrder);

                return ResponseEntity.ok(Map.of(
                        "message", "Đã thêm món vào đơn hiện tại!",
                        "orderId", savedOrder.getId(),
                        "finalPrice", savedOrder.getFinalPrice()
                ));
            }
        }

        // =================================================================================
        // TẠO ĐƠN MỚI
        // =================================================================================
        Order order = new Order();
        order.setUserId(user.getId());
        order.setCustomerName(user.getFullName());
        order.setCreatedAt(LocalDateTime.now());
        order.setAddress(req.getAddress());

        order.setOrderType(req.getOrderType());
        order.setDeliveryAddress(req.getDeliveryAddress());
        order.setCustomerPhone(req.getPhone());

        if (req.getDesiredTime() != null && !req.getDesiredTime().isEmpty()) {
            try {
                order.setDesiredTime(LocalDateTime.parse(req.getDesiredTime()));
            } catch (Exception e) { }
        }

        if ("DELIVERY".equalsIgnoreCase(req.getOrderType())) {
            order.setStatus("WAITING_CONFIRM");
            order.setTableNumber(0);
        } else {
            order.setStatus("PENDING");
            order.setTableNumber(req.getTableNumber());
        }

        order.setNote(req.getNote());

        if (req.getRestaurantId() != null) {
            order.setRestaurantId(req.getRestaurantId());
            restaurantRepository.findById(req.getRestaurantId()).ifPresent(res ->
                    order.setRestaurantName(res.getName())
            );
        } else {
            order.setRestaurantName(req.getRestaurantName());
        }

        double originalTotal = req.getTotal();
        order.setTotalPrice(originalTotal);

        int pointsToUse = req.getPointsToUse() != null ? req.getPointsToUse() : 0;
        double discount = 0;

        if (pointsToUse > 0) {
            int currentPoints = user.getPoints() == null ? 0 : user.getPoints();
            if (currentPoints < pointsToUse) {
                return ResponseEntity.badRequest().body("Bạn không đủ điểm để sử dụng!");
            }
            discount = pointsToUse * 1000;
            user.setPoints(currentPoints - pointsToUse);
            userRepository.save(user);

            order.setPointsUsed(pointsToUse);
            String currentNote = order.getNote() == null ? "" : order.getNote();
            order.setNote(currentNote + " [Dùng " + pointsToUse + " điểm (-" + (long)discount + "đ)]");
        } else {
            order.setPointsUsed(0);
        }

        double finalPrice = originalTotal - discount;
        if (finalPrice < 0) finalPrice = 0;
        order.setFinalPrice(finalPrice);

        List<OrderItem> items = new ArrayList<>();
        if (req.getItems() != null) {
            for (var i : req.getItems()) {
                OrderItem item = new OrderItem();
                item.setItemName(i.getName());
                item.setQuantity(i.getQty());
                item.setPrice(i.getPrice());
                item.setOrder(order);
                item.setStatus("PENDING");
                items.add(item);
            }
        }
        order.setItems(items);

        Order savedOrder = orderRepository.save(order);

        return ResponseEntity.ok(Map.of(
                "message", "Đặt hàng thành công!",
                "orderId", savedOrder.getId(),
                "finalPrice", finalPrice
        ));
    }

    // 2. API LẤY DANH SÁCH ĐƠN HÀNG
    @GetMapping("/my-orders/{userId}")
    public List<Order> getMyOrders(@PathVariable Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // 3. API KHÁCH GỌI THANH TOÁN
    @PutMapping("/{id}/request-payment")
    public ResponseEntity<?> requestPayment(@PathVariable Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

        if ("COMPLETED".equals(order.getStatus()) || "CANCELLED".equals(order.getStatus())) {
            return ResponseEntity.badRequest().body("Đơn hàng đã đóng, không thể gọi thanh toán.");
        }

        order.setStatus("PAYMENT_REQUEST");
        orderRepository.save(order);

        return ResponseEntity.ok(Map.of("message", "Đã gửi yêu cầu thanh toán!"));
    }

    // =================================================================
    // 🔥 4. API CHO STAFF: CẬP NHẬT TRẠNG THÁI (QUAN TRỌNG ĐỂ GIẢI PHÓNG BÀN)
    // =================================================================
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String newStatus = body.get("status");
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        // 1. Cập nhật trạng thái Order
        order.setStatus(newStatus);
        orderRepository.save(order);

        // 2. 🔥 LOGIC ĐỒNG BỘ: NẾU ĐƠN XONG -> ĐÓNG LUÔN BOOKING TẠI BÀN ĐÓ 🔥
        if ("COMPLETED".equals(newStatus) && order.getTableNumber() > 0) {

            // Tìm Booking đang treo ở bàn này (trong ngày hôm nay)
            Optional<Booking> activeBooking = bookingRepository.findActiveBookingAtTable(
                    order.getRestaurantId(),
                    order.getTableNumber(),
                    LocalDate.now()
            );

            if (activeBooking.isPresent()) {
                Booking booking = activeBooking.get();
                booking.setStatus("COMPLETED"); // Chuyển booking sang COMPLETED để Repository coi là bàn trống
                bookingRepository.save(booking);
                System.out.println("✅ Đã đóng Booking #" + booking.getId() + " theo đơn hàng #" + order.getId());
            }
        }

        return ResponseEntity.ok(Map.of("message", "Cập nhật trạng thái thành công!"));
    }
}