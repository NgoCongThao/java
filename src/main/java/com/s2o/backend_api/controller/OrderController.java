package com.s2o.backend_api.controller;

import com.s2o.backend_api.dto.OrderRequest;
import com.s2o.backend_api.entity.Order;
import com.s2o.backend_api.entity.OrderItem;
import com.s2o.backend_api.entity.User;
import com.s2o.backend_api.repository.OrderRepository;
import com.s2o.backend_api.repository.RestaurantRepository;
import com.s2o.backend_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    // 1. API TẠO ĐƠN HÀNG (Dành cho trang Menu)
    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest req) {
        // 1. Lấy User từ Security Context (Token)
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = new Order();
        // Lấy ID từ user trong DB (an toàn hơn lấy từ req)
        order.setUserId(user.getId());
        order.setCustomerName(user.getFullName());

        // Lưu ngày tạo
        order.setCreatedAt(LocalDateTime.now());
        order.setAddress(req.getAddress());

        // --- MAP CÁC TRƯỜNG DELIVERY ---
        order.setOrderType(req.getOrderType()); // "DINE_IN" hoặc "DELIVERY"
        order.setDeliveryAddress(req.getDeliveryAddress());
        order.setCustomerPhone(req.getPhone());

        // Parse giờ hẹn
        if (req.getDesiredTime() != null && !req.getDesiredTime().isEmpty()) {
            try {
                order.setDesiredTime(LocalDateTime.parse(req.getDesiredTime()));
            } catch (Exception e) { }
        }

        // --- PHÂN LOẠI TRẠNG THÁI ---
        if ("DELIVERY".equalsIgnoreCase(req.getOrderType())) {
            order.setStatus("WAITING_CONFIRM");
            order.setTableNumber(0);
        } else {
            order.setStatus("PENDING");
            order.setTableNumber(req.getTableNumber());
        }

        order.setNote(req.getNote());

        // Map Restaurant
        if (req.getRestaurantId() != null) {
            order.setRestaurantId(req.getRestaurantId());
            restaurantRepository.findById(req.getRestaurantId()).ifPresent(res ->
                    order.setRestaurantName(res.getName())
            );
        } else {
            order.setRestaurantName(req.getRestaurantName());
        }

        // ============================================================
        // 🔥 LOGIC TÍNH TIỀN & TRỪ ĐIỂM (BẠN ĐANG THIẾU ĐOẠN NÀY) 🔥
        // ============================================================

        System.out.println("DEBUG: Points to use from Client = " + req.getPointsToUse());

        double originalTotal = req.getTotal();
        order.setTotalPrice(originalTotal); // Giá gốc

        int pointsToUse = req.getPointsToUse() != null ? req.getPointsToUse() : 0;
        double discount = 0;

        // Nếu khách dùng điểm -> Trừ điểm & Tính tiền giảm
        if (pointsToUse > 0) {
            int currentPoints = user.getPoints() == null ? 0 : user.getPoints();

            // Validate: Không được dùng quá số điểm hiện có
            if (currentPoints < pointsToUse) {
                return ResponseEntity.badRequest().body("Bạn không đủ điểm để sử dụng!");
            }

            // Quy đổi: 1 điểm = 1.000đ
            discount = pointsToUse * 1000;

            // Cập nhật User trong DB (Trừ điểm ngay lập tức)
            user.setPoints(currentPoints - pointsToUse);
            userRepository.save(user); // <--- QUAN TRỌNG: LƯU USER LẠI

            // Lưu thông tin vào đơn
            order.setPointsUsed(pointsToUse);

            // Ghi chú thêm vào đơn
            String currentNote = order.getNote() == null ? "" : order.getNote();
            order.setNote(currentNote + " [Dùng " + pointsToUse + " điểm (-" + (long)discount + "đ)]");
        } else {
            order.setPointsUsed(0);
        }

        // Tính Final Price (Giá thực thu)
        double finalPrice = originalTotal - discount;
        if (finalPrice < 0) finalPrice = 0;
        order.setFinalPrice(finalPrice);
        // ============================================================


        // Lưu danh sách món
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

        // Trả về kèm orderId và finalPrice
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

    // 3. API GỌI THANH TOÁN
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
}