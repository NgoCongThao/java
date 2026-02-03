package com.s2o.backend_api.controller;

import com.s2o.backend_api.entity.OrderItem;
import com.s2o.backend_api.entity.User;
import com.s2o.backend_api.repository.OrderItemRepository;
import com.s2o.backend_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/kitchen")
public class KitchenController {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private UserRepository userRepository;

    // --- HÀM HELPER: Lấy ID quán của đầu bếp đang đăng nhập ---
    // (Viết 1 lần dùng cho tất cả các API bên dưới)
    private Long getCurrentRestaurantId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User kitchenStaff = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin nhân viên bếp!"));
        return kitchenStaff.getRestaurantId();
    }

    // ==========================================
    // 1. LẤY DANH SÁCH MÓN CẦN NẤU (PENDING)
    // ==========================================
    // API này trả về danh sách các món lẻ (Ví dụ: 2 Burger, 1 Pizza) thay vì trả về cả đơn hàng.
    // Giúp màn hình bếp hiển thị rõ ràng từng dòng.
    @GetMapping("/items/pending")
    public ResponseEntity<List<OrderItem>> getPendingItems() {
        Long restaurantId = getCurrentRestaurantId();

        // Gọi Query xịn xò chúng ta vừa viết ở Repository
        List<OrderItem> pendingItems = orderItemRepository.findPendingItemsByRestaurant(restaurantId);

        return ResponseEntity.ok(pendingItems);
    }

    // ==========================================
    // 2. BÁO CÁO: ĐÃ NẤU XONG (READY)
    // ==========================================
    // Khi đầu bếp nấu xong, họ bấm nút "Xong" trên màn hình -> Gọi API này
    @PutMapping("/items/{itemId}/ready")
    public ResponseEntity<?> markItemReady(@PathVariable Long itemId) {
        Long restaurantId = getCurrentRestaurantId();

        OrderItem item = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Món ăn không tồn tại"));

        // Bảo mật: Kiểm tra xem món này có đúng là của quán mình không
        // (Tránh trường hợp hack ID để sửa món quán khác)
        if (!item.getOrder().getRestaurantId().equals(restaurantId)) {
            return ResponseEntity.status(403).body("Bạn không có quyền sửa món của quán khác!");
        }

        // Cập nhật trạng thái
        item.setStatus("READY");
        orderItemRepository.save(item);

        return ResponseEntity.ok(Map.of("message", "Đã báo món " + item.getItemName() + " sẵn sàng phục vụ!"));
    }

    // ==========================================
    // 3. BÁO CÁO: HẾT MÓN (OUT OF STOCK) - (Nâng cao)
    // ==========================================
    // Nếu đang nấu mà hết nguyên liệu, bếp có thể hủy món này
    @PutMapping("/items/{itemId}/cancel")
    public ResponseEntity<?> cancelItem(@PathVariable Long itemId, @RequestBody Map<String, String> body) {
        Long restaurantId = getCurrentRestaurantId();
        OrderItem item = orderItemRepository.findById(itemId).orElseThrow();

        if (!item.getOrder().getRestaurantId().equals(restaurantId)) {
            return ResponseEntity.status(403).build();
        }

        item.setStatus("CANCELLED");
        // Lưu lý do hủy (ví dụ: Hết thịt bò) vào note nếu cần (em cần thêm field vào entity nếu muốn)
        orderItemRepository.save(item);

        return ResponseEntity.ok(Map.of("message", "Đã hủy món này!"));
    }
}