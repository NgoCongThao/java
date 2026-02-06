package com.s2o.backend_api.controller;

import com.s2o.backend_api.dto.OrderRequest;
import com.s2o.backend_api.entity.*;
import com.s2o.backend_api.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/guest")
@CrossOrigin(origins = "*")
public class GuestController {

    @Autowired private OrderRepository orderRepository;
    @Autowired private RestaurantRepository restaurantRepository;

    // 1. TẠO ĐƠN HOẶC GỘP ĐƠN (KHÔNG CẦN LOGIN)
    @PostMapping("/orders/create")
    public ResponseEntity<?> createGuestOrder(@RequestBody OrderRequest req) {

        // =====================================================================
        // 🔥 A. LOGIC GỘP ĐƠN: KIỂM TRA BÀN ĐANG CÓ KHÁCH KHÔNG 🔥
        // =====================================================================
        if (req.getTableNumber() != null && req.getTableNumber() > 0 && req.getRestaurantId() != null) {
            List<String> activeStatuses = Arrays.asList("PENDING", "COOKING", "DELIVERING", "READY");

            // Tìm đơn cũ tại Bàn X, Nhà hàng Y, Trạng thái đang ăn
            Optional<Order> existingOrderOpt = orderRepository.findFirstByRestaurantIdAndTableNumberAndStatusIn(
                    req.getRestaurantId(),
                    req.getTableNumber(),
                    activeStatuses
            );

            if (existingOrderOpt.isPresent()) {
                System.out.println("✅ GUEST: GỘP ĐƠN VÀO ORDER ID: " + existingOrderOpt.get().getId());
                Order existingOrder = existingOrderOpt.get();

                // 1. Cộng món mới
                if (req.getItems() != null) {
                    for (var i : req.getItems()) {
                        OrderItem item = new OrderItem();
                        item.setItemName(i.getName());
                        item.setQuantity(i.getQty());
                        item.setPrice(i.getPrice());
                        item.setOrder(existingOrder);
                        item.setStatus("PENDING"); // Món mới -> Bếp phải nấu
                        existingOrder.getItems().add(item);
                    }
                }

                // 2. Cộng tiền
                existingOrder.setTotalPrice(existingOrder.getTotalPrice() + req.getTotal());
                existingOrder.setFinalPrice(existingOrder.getFinalPrice() + req.getTotal());

                // 3. Cập nhật ghi chú (nếu có)
                if (req.getNote() != null && !req.getNote().isEmpty()) {
                    String oldNote = existingOrder.getNote() == null ? "" : existingOrder.getNote();
                    existingOrder.setNote(oldNote + " | Khách gọi thêm: " + req.getNote());
                }

                // 4. Nếu đơn đang READY (đã ra hết món đợt trước), chuyển về PENDING để bếp biết có món mới
                if ("READY".equals(existingOrder.getStatus())) {
                    existingOrder.setStatus("PENDING");
                }

                orderRepository.save(existingOrder);
                return ResponseEntity.ok(Map.of("message", "Đã thêm món vào đơn hiện tại!", "orderId", existingOrder.getId()));
            }
        }

        // =====================================================================
        // 🔥 B. LOGIC TẠO ĐƠN MỚI (NẾU KHÔNG TÌM THẤY ĐƠN CŨ) 🔥
        // =====================================================================
        // (Phần này bạn bị thiếu trong code gửi lên, bắt buộc phải có để tạo đơn lần đầu)

        Order order = new Order();
        order.setCreatedAt(LocalDateTime.now());
        order.setCustomerName(req.getCustomerName() != null ? req.getCustomerName() : "Khách vãng lai");
        order.setCustomerPhone(req.getPhone());
        order.setAddress(req.getAddress());
        order.setOrderType("DINE_IN"); // Mặc định Guest quét QR là ăn tại bàn
        order.setStatus("PENDING");
        order.setTableNumber(req.getTableNumber());
        order.setNote(req.getNote());

        // Set User ID nếu có (trường hợp user login nhưng quét QR guest)
        if (req.getUserId() != null) {
            order.setUserId(req.getUserId());
        }

        // Map thông tin nhà hàng
        if (req.getRestaurantId() != null) {
            order.setRestaurantId(req.getRestaurantId());
            restaurantRepository.findById(req.getRestaurantId()).ifPresent(res ->
                    order.setRestaurantName(res.getName())
            );
        }

        order.setTotalPrice(req.getTotal());
        order.setFinalPrice(req.getTotal()); // Guest thường không có điểm thưởng

        // Map danh sách món
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
                "message", "Đặt món thành công!",
                "orderId", savedOrder.getId()
        ));
    } // <--- ĐÓNG NGOẶC HÀM createGuestOrder (Lỗi của bạn nằm ở việc thiếu cái này)


    // 2. LẤY THÔNG TIN ĐƠN (ĐỂ TRACKING)
    @GetMapping("/orders/{id}")
    public ResponseEntity<?> getGuestOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Không tìm thấy đơn hàng")
        ));
    }

    // 3. KHÁCH GỌI THANH TOÁN
    @PutMapping("/orders/{id}/request-payment")
    public ResponseEntity<?> requestPayment(@PathVariable Long id) {
        Order order = orderRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Không tìm thấy đơn hàng")
        );

        if ("COMPLETED".equals(order.getStatus()) || "CANCELLED".equals(order.getStatus())) {
            return ResponseEntity.badRequest().body("Đơn hàng đã đóng.");
        }

        order.setStatus("PAYMENT_REQUEST");
        orderRepository.save(order);

        return ResponseEntity.ok(Map.of("message", "Đã gọi nhân viên thanh toán!"));
    }
}