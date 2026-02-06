package com.s2o.backend_api.controller;

import com.s2o.backend_api.entity.Restaurant;
import com.s2o.backend_api.repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/restaurants") // Giữ nguyên prefix chung
@CrossOrigin(origins = "*")
public class RestaurantController {

    @Autowired
    private RestaurantRepository restaurantRepository;

    // --- GUEST API (KHÁCH HÀNG) ---
    // Chỉ giữ lại các API cho người dùng xem, không sửa xóa gì ở đây
    
    // 1. Lấy danh sách nhà hàng
    // URL: http://localhost:8080/api/guest/restaurants
    @GetMapping("/guest/restaurants")
    public List<Restaurant> getAllRestaurants() {
        return restaurantRepository.findAll();
    }

    // 2. Lấy chi tiết 1 nhà hàng (API này hữu ích cho trang detail)
    // URL: http://localhost:8080/api/guest/restaurants/{id}
    @GetMapping("/guest/restaurants/{id}")
    public ResponseEntity<?> getRestaurantById(@PathVariable Long id) {
        Optional<Restaurant> res = restaurantRepository.findById(id);
        if(res.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(res.get());
    }
    // 👇 THÊM API NÀY: Lấy danh sách quán đang ACTIVE 👇
    @GetMapping("/public")
    public ResponseEntity<?> getActiveRestaurants() {
        // Giả sử bạn có trường 'status' trong Entity Restaurant.
        // Nếu chưa có hàm findByStatus, hãy thêm vào Repository (xem bước 1.1 bên dưới)
        List<Restaurant> list = restaurantRepository.findByStatus("ACTIVE");
        return ResponseEntity.ok(list);
    }
    // ❌ ĐÃ XÓA: createRestaurant (Đã có bên AdminController)
    // ❌ ĐÃ XÓA: updateRestaurant (Đã có bên AdminController)
    // ❌ ĐÃ XÓA: deleteRestaurant (Đã có bên AdminController)
}