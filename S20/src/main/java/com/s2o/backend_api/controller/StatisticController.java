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

    // Tuấn thêm đoạn code này để tính doanh thu theo khoảng thời gian
   @GetMapping("/partner/{restaurantId}/range")
public ResponseEntity<?> getRevenueByRange(
    @PathVariable Long restaurantId,
    @RequestParam String startDate, 
    @RequestParam String endDate
) {
    // Tạo chuỗi thời gian chuẩn SQL Server
    String start = startDate + " 00:00:00";
    String end = endDate + " 23:59:59";

    // Gọi hàm lọc theo ngày, KHÔNG gọi hàm tính tổng toàn bộ
    Double revenue = orderRepository.calculateRevenueByRange(restaurantId, start, end);

    return ResponseEntity.ok(Map.of("revenue", revenue != null ? revenue : 0.0));
}

// Tuấn thêm đoạn code này để tính tổng doanh thu
@GetMapping("/partner/{restaurantId}")
public ResponseEntity<?> getPartnerStats(@PathVariable Long restaurantId) {
    // 1. Tính tổng toàn bộ để hiện ở Dashboard
    Double grandTotal = orderRepository.calculateGrandTotalRevenue(restaurantId);
    
    // 2. Tính số lượng món ăn và nhân viên (nếu cần)
    // ... logic khác ...

    Map<String, Object> response = new HashMap<>();
    response.put("totalRevenue", grandTotal != null ? grandTotal : 0.0);
    
    return ResponseEntity.ok(response);
}
}
