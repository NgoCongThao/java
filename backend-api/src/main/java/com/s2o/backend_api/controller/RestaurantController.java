package com.s2o.backend_api.controller;
import org.springframework.http.ResponseEntity;
import com.s2o.backend_api.entity.Restaurant;
import com.s2o.backend_api.repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/guest")
@CrossOrigin(origins = "*")
public class RestaurantController {

    @Autowired
    private RestaurantRepository restaurantRepository;

    // API: Lấy danh sách tất cả nhà hàng
    // URL: http://localhost:8080/api/guest/restaurants
    @GetMapping("/restaurants")
    public List<Restaurant> getAllRestaurants() {
        return restaurantRepository.findAll();
    }
    // --- ADMIN API ---

    // 1. Thêm nhà hàng mới
    @PostMapping("/admin/restaurants")
    public ResponseEntity<Restaurant> createRestaurant(@RequestBody Restaurant restaurant) {
        return ResponseEntity.ok(restaurantRepository.save(restaurant));
    }

    // 2. Cập nhật nhà hàng (Vị trí, Số bàn, Tên...)
    @PutMapping("/admin/restaurants/{id}")
    public ResponseEntity<?> updateRestaurant(@PathVariable Long id, @RequestBody Restaurant request) {
        return restaurantRepository.findById(id).map(restaurant -> {
            restaurant.setName(request.getName());
            restaurant.setAddress(request.getAddress());
           // Nếu trong Entity bạn đặt là 'image' thì sửa thành:
            restaurant.setImage(request.getImage()); 

// HOẶC nếu Entity bạn đặt là 'imageUrl' (nhưng chưa có getter) thì phải vào file Entity để thêm getter.
// Nhưng khả năng cao là trường hợp 1.// Sửa lại getter/setter nếu tên trường trong Entity khác
            restaurant.setLatitude(request.getLatitude());   // Cập nhật Vị trí
            restaurant.setLongitude(request.getLongitude()); // Cập nhật Vị trí
            restaurant.setTotalTables(request.getTotalTables()); // Cập nhật Số bàn
            restaurant.setIsOpen(request.getIsOpen());
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