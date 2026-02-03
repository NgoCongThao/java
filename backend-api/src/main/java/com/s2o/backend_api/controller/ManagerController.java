package com.s2o.backend_api.controller;

import com.s2o.backend_api.dto.RegisterRequest;
import com.s2o.backend_api.entity.User;
import com.s2o.backend_api.repository.MenuItemRepository;
import com.s2o.backend_api.repository.OrderRepository;
import com.s2o.backend_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;


    @RestController
    @RequestMapping("/api/manager")
    public class ManagerController {

        @Autowired
        private UserRepository userRepository;
        @Autowired private OrderRepository orderRepository;
        @Autowired private MenuItemRepository menuItemRepository;
        @Autowired private PasswordEncoder passwordEncoder;

        // Helper: Lấy ID quán của Manager đang đăng nhập
        private Long getCurrentRestaurantId() {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByUsername(username).get();
            return user.getRestaurantId();
        }

        // 1. Xem doanh thu quán mình
        @GetMapping("/revenue")
        public ResponseEntity<?> getMyRevenue() {
            Double revenue = orderRepository.calculateRevenueByRestaurant(getCurrentRestaurantId());
            return ResponseEntity.ok(revenue != null ? revenue : 0.0);
        }

        // 2. Tạo nhân viên (Staff/Kitchen)
        @PostMapping("/create-staff")
        public ResponseEntity<?> createStaff(@RequestBody RegisterRequest req) {
            User staff = new User();
            staff.setUsername(req.getUsername());
            staff.setPassword(passwordEncoder.encode(req.getPassword()));
            staff.setRole(req.getRole()); // "STAFF" hoặc "KITCHEN"
            staff.setRestaurantId(getCurrentRestaurantId()); // Quan trọng: Nhân viên phải cùng quán với chủ
            userRepository.save(staff);
            return ResponseEntity.ok("Tạo nhân viên thành công!");
        }

        // 3. Quản lý Menu (Thêm/Sửa/Xóa món) - Có thể tái sử dụng MenuController hoặc viết mới tại đây
    }

