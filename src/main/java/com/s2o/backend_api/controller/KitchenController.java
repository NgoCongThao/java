package com.s2o.backend_api.controller;

import com.s2o.backend_api.entity.Order;
import com.s2o.backend_api.entity.OrderItem;
import com.s2o.backend_api.entity.User;
import com.s2o.backend_api.repository.OrderItemRepository;
import com.s2o.backend_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/kitchen")
public class KitchenController {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private com.s2o.backend_api.repository.OrderRepository orderRepository;
    // Helper: Lấy ID quán từ tài khoản bếp đang đăng nhập
    private Long getCurrentRestaurantId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User kitchenStaff = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin nhân viên bếp!"));
        return kitchenStaff.getRestaurantId();
    }

    // ==========================================
    // 1. LẤY DANH SÁCH MÓN CẦN NẤU (PENDING)
    // ==========================================
    @GetMapping("/items/pending")
    public ResponseEntity<List<Map<String, Object>>> getPendingItems() {
        Long restaurantId = getCurrentRestaurantId();

        // Gọi hàm vừa thêm trong Repository
        List<OrderItem> pendingItems = orderItemRepository.findPendingItemsByRestaurant(restaurantId);

        List<Map<String, Object>> responseList = new ArrayList<>();

        for (OrderItem item : pendingItems) {
            Map<String, Object> itemData = new HashMap<>();

            // --- CẤP 1: THÔNG TIN MÓN ĂN ---
            itemData.put("id", item.getId());
            // Frontend đang dùng biến 'menuItemName'
            itemData.put("menuItemName", item.getItemName());
            itemData.put("quantity", item.getQuantity());
            itemData.put("status", item.getStatus());

            // --- CẤP 2: THÔNG TIN ĐƠN HÀNG (Lồng bên trong) ---
            Order order = item.getOrder();
            if (order != null) {
                Map<String, Object> orderData = new HashMap<>();
                orderData.put("id", order.getId());
                orderData.put("tableNumber", order.getTableNumber());

                // Xử lý Ghi chú thông minh
                String typeNote = "";
                if ("DELIVERY".equals(order.getOrderType())) {
                    typeNote = " 📦 GIAO ĐI - ĐÓNG HỘP";
                } else if ("TAKEAWAY".equals(order.getOrderType())) {
                    typeNote = " 🥡 MANG VỀ";
                }

                // Gộp ghi chú khách + ghi chú hệ thống
                String finalNote = (order.getNote() != null ? order.getNote() : "") + typeNote;
                orderData.put("note", finalNote);

                // Nhét orderData vào trong itemData để Frontend gọi item.order.note
                itemData.put("order", orderData);
            }

            responseList.add(itemData);
        }

        return ResponseEntity.ok(responseList);
    }

    // ==========================================
    // 2. BÁO CÁO: ĐÃ NẤU XONG (READY)
    // ==========================================
    @PutMapping("/items/{itemId}/ready")
    public ResponseEntity<?> markItemReady(@PathVariable Long itemId) {
        Long restaurantId = getCurrentRestaurantId();
        OrderItem item = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Món ăn không tồn tại"));

        // 1. Đổi trạng thái món hiện tại
        item.setStatus("READY");
        orderItemRepository.save(item);

        Order order = item.getOrder();

        // 2. LOGIC CẬP NHẬT TRẠNG THÁI ĐƠN HÀNG
        // Nếu là đơn Giao hàng (DELIVERY) -> Logic cũ (Xong hết mới Completed)
        if ("DELIVERY".equalsIgnoreCase(order.getOrderType())) {
            List<OrderItem> allItems = orderItemRepository.findByOrderId(order.getId());
            boolean allDone = allItems.stream()
                    .allMatch(i -> "READY".equals(i.getStatus()) || "CANCELLED".equals(i.getStatus()));
            if (allDone) {
                order.setStatus("COMPLETED");
                orderRepository.save(order); // <-- Nhớ save
            }
        }
        // Nếu là đơn Ăn tại bàn (DINE_IN) -> Chỉ cần 1 món xong là đổi sang DELIVERING
        else if ("DINE_IN".equalsIgnoreCase(order.getOrderType())) {
            if ("PENDING".equals(order.getStatus())) {
                order.setStatus("DELIVERING"); // Đang lên món
                orderRepository.save(order);
            }
        }

        return ResponseEntity.ok(Map.of("message", "Món đã xong!"));
    }

    // ==========================================
    // 3. BÁO CÁO: HẾT MÓN (OUT OF STOCK)
    // ==========================================
    @PutMapping("/items/{itemId}/cancel")
    public ResponseEntity<?> cancelItem(@PathVariable Long itemId, @RequestBody Map<String, String> body) {
        Long restaurantId = getCurrentRestaurantId();
        OrderItem item = orderItemRepository.findById(itemId).orElseThrow();

        if (!item.getOrder().getRestaurantId().equals(restaurantId)) {
            return ResponseEntity.status(403).build();
        }

        item.setStatus("CANCELLED");
        orderItemRepository.save(item);

        return ResponseEntity.ok(Map.of("message", "Đã hủy món này!"));
    }
}