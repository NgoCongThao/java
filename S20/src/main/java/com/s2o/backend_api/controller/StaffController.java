package com.s2o.backend_api.controller;

import com.s2o.backend_api.entity.Order;
import com.s2o.backend_api.entity.User;
import com.s2o.backend_api.repository.OrderRepository;
import com.s2o.backend_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff")
@CrossOrigin(origins = "*")
public class StaffController {

    @Autowired
    private OrderRepository orderRepository; // Cần viết thêm hàm query trong Repository

    @Autowired
    private UserRepository userRepository;

    // 1. Lấy danh sách món Bếp đã nấu xong (READY) để bưng ra bàn
    @GetMapping("/ready-orders")
    public ResponseEntity<?> getReadyOrders() {
        User staff = getCurrentUser();
        if (staff == null || staff.getRestaurantId() == null) {
            return ResponseEntity.badRequest().body("Lỗi xác thực nhân viên!");
        }

        // Giả sử status quy trình: PENDING -> PREPARING (Bếp đang nấu) -> READY (Bếp nấu xong) -> SERVED (Đã ra bàn)
        // Bạn cần thêm hàm findByRestaurantIdAndStatus vào OrderRepository nhé
        List<Order> readyOrders = orderRepository.findByRestaurantIdAndStatus(staff.getRestaurantId(), "READY");

        return ResponseEntity.ok(readyOrders);
    }

    // 2. Xác nhận đã mang món ra bàn (SERVED)
    @PutMapping("/serve/{orderId}")
    public ResponseEntity<?> markAsServed(@PathVariable Long orderId) {
        return orderRepository.findById(orderId).map(order -> {
            if ("READY".equals(order.getStatus())) {
                order.setStatus("SERVED");
                orderRepository.save(order);
                return ResponseEntity.ok("Đã cập nhật: Món đã được phục vụ!");
            }
            return ResponseEntity.badRequest().body("Món ăn chưa sẵn sàng hoặc đã phục vụ rồi!");
        }).orElse(ResponseEntity.notFound().build());
    }

    // Hàm phụ lấy User hiện tại từ Token
    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username).orElse(null);
    }
}