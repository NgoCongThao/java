package com.s2o.backend_api.controller;

import com.s2o.backend_api.entity.Restaurant;
import com.s2o.backend_api.repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api") // SỬA: Chỉ để /api, bỏ /guest
@CrossOrigin(origins = "*")
public class RestaurantController {

    @Autowired
    private RestaurantRepository restaurantRepository;

    // --- GUEST API ---
    // URL: http://localhost:8080/api/guest/restaurants
    // Lấy danh sách tất cả nhà hàng 
    @GetMapping("/guest/restaurants")
    public List<Restaurant> getAllRestaurants() {
        return restaurantRepository.findAll();
    }

    // --- ADMIN API ---

    // 1. Thêm nhà hàng mới
    @PostMapping("/admin/restaurants")
    public ResponseEntity<Restaurant> createRestaurant(@RequestBody Restaurant restaurant) {
        // Chỉ gán mặc định các trường khác, còn Rating thì lấy từ người dùng nhập
        if (restaurant.getStatus() == null || restaurant.getStatus().isEmpty()) {
            restaurant.setStatus("active");
        }
        if (restaurant.getCategory() == null) {
            restaurant.setCategory("Món ăn");
        }
        // Nếu người dùng quên nhập rating thì mới để mặc định là 0, còn nhập rồi thì giữ nguyên
        if (restaurant.getRating() == null) {
            restaurant.setRating(0.0); 
        }

        return ResponseEntity.ok(restaurantRepository.save(restaurant));
    }

    // 2. Cập nhật nhà hàng
    @PutMapping("/admin/restaurants/{id}")
    public ResponseEntity<?> updateRestaurant(@PathVariable Long id, @RequestBody Restaurant request) {
        return restaurantRepository.findById(id).map(restaurant -> {
            restaurant.setName(request.getName());
            restaurant.setAddress(request.getAddress());
            restaurant.setImage(request.getImage()); 
            restaurant.setLatitude(request.getLatitude());
            restaurant.setLongitude(request.getLongitude());
            restaurant.setTotalTables(request.getTotalTables());
            restaurant.setIsOpen(request.getIsOpen());
            
            // Cập nhật các trường phụ
            restaurant.setCategory(request.getCategory());
            restaurant.setTime(request.getTime());
            restaurant.setDescription(request.getDescription());
            restaurant.setStatus(request.getStatus());
            
            // QUAN TRỌNG: Cho phép cập nhật Rating
            if (request.getRating() != null) {
                restaurant.setRating(request.getRating());
            }

            return ResponseEntity.ok(restaurantRepository.save(restaurant));
        }).orElse(ResponseEntity.notFound().build());
    }
    // 3. Xóa nhà hàng
    @DeleteMapping("/admin/restaurants/{id}")
    public ResponseEntity<?> deleteRestaurant(@PathVariable Long id) {
        if (!restaurantRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        restaurantRepository.deleteById(id);
        return ResponseEntity.ok("Đã xóa nhà hàng thành công!");
    }
}