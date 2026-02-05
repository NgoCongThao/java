package com.s2o.backend_api.controller;

import com.s2o.backend_api.dto.RegisterRequest;
import com.s2o.backend_api.entity.MenuItem;
import com.s2o.backend_api.entity.Restaurant;
import com.s2o.backend_api.entity.User;
import com.s2o.backend_api.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/manager")
public class ManagerController {

    @Autowired private UserRepository userRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private RestaurantRepository restaurantRepository;
    @Autowired private MenuItemRepository menuItemRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    // Helper: Lấy ID quán của Manager đang đăng nhập
    private Long getCurrentRestaurantId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .map(User::getRestaurantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy User!"));
    }

    // ================= 1. THỐNG KÊ (DASHBOARD) =================
    @GetMapping("/stats")
    public ResponseEntity<?> getDashboardStats() {
        Long resId = getCurrentRestaurantId();
        Map<String, Object> stats = new HashMap<>();

        // Doanh thu tổng
        Double revenue = orderRepository.calculateRevenueByRestaurant(resId);
        stats.put("revenue", revenue != null ? revenue : 0.0);

        // Số đơn hàng hôm nay (Ví dụ đơn giản, em có thể query theo ngày nếu muốn)
        stats.put("totalOrders", orderRepository.findByRestaurantIdOrderByCreatedAtDesc(resId).size());

        // Top món bán chạy (Lấy 5 món đầu)
        List<Object[]> topItems = orderItemRepository.findTopSellingItems(resId);
        stats.put("topItems", topItems.stream().limit(5).toList());

        return ResponseEntity.ok(stats);
    }

    // ================= 2. QUẢN LÝ QUÁN (MY RESTAURANT) =================
    @GetMapping("/restaurant")
    public ResponseEntity<?> getMyRestaurant() {
        Long resId = getCurrentRestaurantId();
        return ResponseEntity.ok(restaurantRepository.findById(resId).orElseThrow());
    }

    @PutMapping("/restaurant")
    public ResponseEntity<?> updateMyRestaurant(@RequestBody Restaurant req) {
        Long resId = getCurrentRestaurantId();
        return restaurantRepository.findById(resId).map(res -> {
            // Chỉ cho sửa các thông tin cơ bản
            res.setName(req.getName());
            res.setAddress(req.getAddress());
            res.setPhone(req.getPhone());
            res.setImage(req.getImage());
            res.setDescription(req.getDescription());
            res.setIsOpen(req.getIsOpen()); // Đóng/Mở cửa quán
            // Không cho sửa Rating hay TotalTables (Admin mới được sửa)
            restaurantRepository.save(res);
            return ResponseEntity.ok("Cập nhật thông tin quán thành công!");
        }).orElse(ResponseEntity.badRequest().build());
    }

    // ================= 3. QUẢN LÝ MENU (MY MENU) =================
    @GetMapping("/menu")
    public List<MenuItem> getMyMenu() {
        return menuItemRepository.findByRestaurantId(getCurrentRestaurantId());
    }

    @PostMapping("/menu")
    public ResponseEntity<?> addMenuItem(@RequestBody MenuItem item) {
        Long resId = getCurrentRestaurantId();
        Restaurant res = restaurantRepository.findById(resId).get();

        item.setRestaurant(res); // Gắn chặt món này vào quán của Manager
        item.setIsAvailable(true);
        menuItemRepository.save(item);
        return ResponseEntity.ok("Thêm món thành công");
    }

    @DeleteMapping("/menu/{id}")
    public ResponseEntity<?> deleteMenuItem(@PathVariable Long id) {
        Long resId = getCurrentRestaurantId();
        MenuItem item = menuItemRepository.findById(id).orElseThrow();

        // BẢO MẬT: Check xem món này có đúng của quán mình không
        if (!item.getRestaurant().getId().equals(resId)) {
            return ResponseEntity.status(403).body("Không được xóa món của quán khác!");
        }
        menuItemRepository.delete(item);
        return ResponseEntity.ok("Đã xóa món ăn");
    }

    // ================= 4. QUẢN LÝ NHÂN SỰ (MY STAFF) =================
    @GetMapping("/staff")
    public List<User> getMyStaff() {
        // Cần thêm hàm findByRestaurantId trong UserRepository nhé
        return userRepository.findAll().stream()
                .filter(u -> getCurrentRestaurantId().equals(u.getRestaurantId())
                        && !"MANAGER".equals(u.getRole())) // Không hiện chính mình
                .toList();
    }

    @PostMapping("/staff")
    public ResponseEntity<?> createStaff(@RequestBody RegisterRequest req) {
        if(userRepository.existsByUsername(req.getUsername())) {
            return ResponseEntity.badRequest().body("Username đã tồn tại");
        }
        User staff = new User();
        staff.setUsername(req.getUsername());
        staff.setPassword(passwordEncoder.encode(req.getPassword()));
        staff.setFullName(req.getFullName());
        staff.setRole(req.getRole()); // STAFF hoặc KITCHEN
        staff.setRestaurantId(getCurrentRestaurantId()); // Cùng quán với chủ

        userRepository.save(staff);
        return ResponseEntity.ok("Tạo nhân viên thành công");
    }

    @DeleteMapping("/staff/{id}")
    public ResponseEntity<?> fireStaff(@PathVariable Long id) {
        Long resId = getCurrentRestaurantId();
        User staff = userRepository.findById(id).orElseThrow();
        if(!staff.getRestaurantId().equals(resId)) {
            return ResponseEntity.status(403).body("Không được xóa nhân viên quán khác!");
        }
        userRepository.delete(staff);
        return ResponseEntity.ok("Đã sa thải nhân viên");
    }
}