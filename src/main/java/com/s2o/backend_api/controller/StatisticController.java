package com.s2o.backend_api.controller;

import com.s2o.backend_api.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@CrossOrigin(origins = "*")
public class StatisticController {

    @Autowired
    private OrderRepository orderRepository;

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserStats(@PathVariable Long userId) {
        // Đếm tổng số đơn hàng của user này
        long totalOrders = orderRepository.countByUserId(userId);
        
        // Tính hạng thành viên
        String rank = "Đồng";
        String nextRank = "Bạc";
        long target = 5;

        if (totalOrders >= 50) {
            rank = "Kim Cương";
            nextRank = "Max";
            target = 0;
        } else if (totalOrders >= 10) {
            rank = "Vàng";
            nextRank = "Kim Cương";
            target = 50;
        } else if (totalOrders >= 5) {
            rank = "Bạc";
            nextRank = "Vàng";
            target = 10;
        }

        Map<String, Object> res = new HashMap<>();
        res.put("totalOrders", totalOrders);
        res.put("rank", rank);
        res.put("nextRank", nextRank);
        res.put("ordersLeft", target - totalOrders); // Số đơn còn thiếu để lên hạng

        return ResponseEntity.ok(res);
    }
}