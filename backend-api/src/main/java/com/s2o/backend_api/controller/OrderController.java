package com.s2o.backend_api.controller;
import com.s2o.backend_api.repository.RestaurantRepository;
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
// THÊM
    @Autowired
    private RestaurantRepository restaurantRepository;

    // 1. API TẠO ĐƠN HÀNG (Dành cho trang Menu)
   @PostMapping("/create")
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest req) {
        Order order = new Order();
        order.setUserId(req.getUserId());
        order.setTotalPrice(req.getTotal());
        order.setAddress(req.getAddress());
        order.setStatus("PENDING");
        order.setTableNumber(req.getTableNumber());
        order.setNote(req.getNote());
        // QUAN TRỌNG: lưu restaurantId và lấy tên nhà hàng từ DB
        if (req.getRestaurantId() != null) {
            order.setRestaurantId(req.getRestaurantId());
            restaurantRepository.findById(req.getRestaurantId()).ifPresent(res -> 
                order.setRestaurantName(res.getName())
            );
        } else {
            // nếu không có id (trường hợp cũ), giữ tên cũ
            order.setRestaurantName(req.getRestaurantName());
        }
        
        // --- DÒNG NÀY SẼ HẾT ĐỎ ---
        order.setNote(req.getNote());
        // --------------------------

        // Lưu danh sách món
        List<OrderItem> items = new ArrayList<>();
        for (OrderRequest.ItemRequest i : req.getItems()) {
            OrderItem item = new OrderItem();
            item.setItemName(i.getName());
            item.setQuantity(i.getQty());
            item.setPrice(i.getPrice());
            item.setOrder(order);
            items.add(item);
        }
        order.setItems(items);

        orderRepository.save(order);
        return ResponseEntity.ok("Đặt hàng thành công!");
    }

    // 2. API LẤY DANH SÁCH ĐƠN HÀNG (Dành cho trang Tracking)
    @GetMapping("/my-orders/{userId}")
    public List<Order> getMyOrders(@PathVariable Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);    }
}