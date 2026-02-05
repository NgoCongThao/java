package com.s2o.backend_api.controller;

import com.s2o.backend_api.dto.OrderRequest;
import com.s2o.backend_api.entity.*;
import com.s2o.backend_api.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/guest")
@CrossOrigin(origins = "*")
public class GuestController {

    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private RestaurantRepository restaurantRepository;

    // 1. TẠO ĐƠN (KHÔNG CẦN LOGIN)
    @PostMapping("/orders/create")
    public ResponseEntity<?> createGuestOrder(@RequestBody OrderRequest req) {
        Order order = new Order();
        order.setUserId(null); // Không có user
        order.setCustomerName("Khách Bàn " + req.getTableNumber()); // Tên tạm

        order.setRestaurantId(req.getRestaurantId());
        restaurantRepository.findById(req.getRestaurantId()).ifPresent(res ->
                order.setRestaurantName(res.getName())
        );

        order.setTableNumber(req.getTableNumber());
        order.setOrderType("DINE_IN");
        order.setStatus("PENDING"); // Vào bếp luôn
        order.setNote(req.getNote());
        order.setTotalPrice(req.getTotal());
        order.setCreatedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);

        List<OrderItem> items = new ArrayList<>();
        for (var i : req.getItems()) {
            OrderItem item = new OrderItem();
            item.setItemName(i.getName());
            item.setQuantity(i.getQty());
            item.setPrice(i.getPrice());
            item.setOrder(savedOrder);
            item.setStatus("PENDING");
            items.add(item);
        }
        orderItemRepository.saveAll(items);

        // Trả về ID để lưu vào LocalStorage
        return ResponseEntity.ok(Map.of("message", "Thành công", "orderId", savedOrder.getId()));
    }

    // 2. LẤY THÔNG TIN ĐƠN (ĐỂ TRACKING)
    @GetMapping("/orders/{id}")
    public ResponseEntity<?> getGuestOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderRepository.findById(id).orElseThrow());
    }

    // 3. KHÁCH GỌI THANH TOÁN
    @PutMapping("/orders/{id}/request-payment")
    public ResponseEntity<?> requestPayment(@PathVariable Long id) {
        Order order = orderRepository.findById(id).orElseThrow();

        if ("COMPLETED".equals(order.getStatus())) {
            return ResponseEntity.badRequest().body("Đơn đã đóng.");
        }

        order.setStatus("PAYMENT_REQUEST");
        orderRepository.save(order);

        return ResponseEntity.ok(Map.of("message", "Đã gọi nhân viên!"));
    }
}