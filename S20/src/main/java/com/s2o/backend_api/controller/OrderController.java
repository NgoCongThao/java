package com.s2o.backend_api.controller;

import com.s2o.backend_api.dto.OrderRequest;
import com.s2o.backend_api.entity.Order;
import com.s2o.backend_api.entity.OrderItem;
import com.s2o.backend_api.repository.OrderRepository; // Bạn tự tạo file Repository nhé (kế thừa JpaRepository)
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    // 1. API TẠO ĐƠN HÀNG (Dành cho trang Menu)
    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest req) {
        Order order = new Order();
        order.setUserId(req.getUserId());
        order.setRestaurantName(req.getRestaurantName());
        order.setAddress(req.getAddress());
        order.setStatus("PENDING"); // Mới đặt thì chờ xác nhận

        // Biến để tự tính tổng tiền tại Backend
        double calculatedTotal = 0;
        
        // Lưu danh sách món
        List<OrderItem> items = new ArrayList<>();
        for (OrderRequest.ItemRequest i : req.getItems()) {
            OrderItem item = new OrderItem();
            item.setItemName(i.getName());
            item.setQuantity(i.getQty());
            item.setPrice(i.getPrice());
            item.setOrder(order);
            items.add(item);

            // --- LOGIC MỚI: TỰ TÍNH TIỀN ---
            // Cộng dồn vào tổng tiền: (Giá x Số lượng)
            calculatedTotal += (i.getPrice() * i.getQty());
        }
        order.setItems(items);

        // --- CẬP NHẬT TỔNG TIỀN CHÍNH XÁC ---
        order.setTotalPrice(calculatedTotal);

        orderRepository.save(order);
        return ResponseEntity.ok("Đặt hàng thành công!");
    }

    // 2. API LẤY DANH SÁCH ĐƠN HÀNG (Dành cho trang Tracking)
    @GetMapping("/my-orders/{userId}")
    public List<Order> getMyOrders(@PathVariable Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);    }
}