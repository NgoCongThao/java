package com.s2o.backend_api.controller;

import com.s2o.backend_api.dto.CreateTenantRequest;
import com.s2o.backend_api.entity.Restaurant;
import com.s2o.backend_api.entity.User;
import com.s2o.backend_api.repository.OrderRepository;
import com.s2o.backend_api.repository.RestaurantRepository;
import com.s2o.backend_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private RestaurantRepository restaurantRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    // 1. Thêm nhà hàng mới + Tạo tài khoản Manager
    @PostMapping("/create-tenant")
    public ResponseEntity<?> createTenant(@RequestBody CreateTenantRequest req) {
        // B1: Tạo nhà hàng
        Restaurant res = new Restaurant();
        res.setName(req.getRestaurantName());
        res.setAddress(req.getAddress());
        res.setPhone(req.getPhone());
        res.setIsOpen(true);
        Restaurant savedRes = restaurantRepository.save(res);

        // B2: Tạo Manager cai quản nhà hàng đó
        User manager = new User();
        manager.setUsername(req.getManagerUsername());
        manager.setPassword(passwordEncoder.encode(req.getManagerPassword()));
        manager.setRole("MANAGER"); // Role mới quyền lực
        manager.setRestaurantId(savedRes.getId()); // Gắn kết Manager với Quán
        manager.setFullName(req.getManagerFullName());

        userRepository.save(manager);
        return ResponseEntity.ok("Đã tạo quán " + savedRes.getName());
    }

    // 2. Khóa nhà hàng (Ngừng hợp tác)
    @PutMapping("/lock/{restaurantId}")
    public ResponseEntity<?> lockRestaurant(@PathVariable Long restaurantId) {
        Restaurant res = restaurantRepository.findById(restaurantId).orElseThrow();
        res.setIsOpen(false); // Đóng cửa
        restaurantRepository.save(res);
        return ResponseEntity.ok("Đã khóa nhà hàng: " + res.getName());
    }

    // 3. Xem doanh số toàn sàn
    @GetMapping("/system-revenue")
    public ResponseEntity<?> getSystemRevenue() {
        Double total = orderRepository.calculateTotalSystemRevenue();
        return ResponseEntity.ok(total != null ? total : 0.0);
    }
}