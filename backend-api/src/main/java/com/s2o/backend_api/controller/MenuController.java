package com.s2o.backend_api.controller;

import com.s2o.backend_api.entity.MenuItem;
import com.s2o.backend_api.repository.MenuItemRepository;
import com.s2o.backend_api.repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api") // SỬA: Chỉ để /api
@CrossOrigin(origins = "*")
public class MenuController {

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    // --- GUEST API ---
    // URL: http://localhost:8080/api/guest/menu/restaurant/1
    @GetMapping("/guest/menu/restaurant/{restaurantId}")
    public List<MenuItem> getMenuByRestaurant(@PathVariable Long restaurantId) {
        return menuItemRepository.findByRestaurantId(restaurantId);
    }

    // --- ADMIN API ---

    // 1. Lấy danh sách món của 1 nhà hàng (Để Admin xem và sửa)
    // URL: http://localhost:8080/api/admin/menu/restaurant/1
    @GetMapping("/admin/menu/restaurant/{restaurantId}")
    public List<MenuItem> getAdminMenu(@PathVariable Long restaurantId) {
        return menuItemRepository.findByRestaurantId(restaurantId);
    }

   // 2. Thêm món ăn
    @PostMapping("/admin/menu")
    public ResponseEntity<?> addMenuItem(@RequestBody MenuItem menuItem) {
        // Kiểm tra xem ID nhà hàng có tồn tại trong cục JSON gửi lên không
        if (menuItem.getRestaurant() == null || menuItem.getRestaurant().getId() == null) {
            return ResponseEntity.badRequest().body("Lỗi: Dữ liệu gửi lên thiếu ID nhà hàng (restaurant.id)");
        }

        Long resId = menuItem.getRestaurant().getId();
        var restaurantOpt = restaurantRepository.findById(resId);
        
        if (restaurantOpt.isPresent()) {
            menuItem.setRestaurant(restaurantOpt.get());
            
            // --- ĐẢM BẢO DỮ LIỆU CÁC CỘT KHÔNG BỊ NULL ---
            if (menuItem.getIsAvailable() == null) menuItem.setIsAvailable(true);
            if (menuItem.getName() == null) menuItem.setName("Món mới chưa đặt tên");
            if (menuItem.getPrice() == null) menuItem.setPrice(0.0);
            if (menuItem.getCategory() == null || menuItem.getCategory().isEmpty()) menuItem.setCategory("Khác");
            if (menuItem.getDescription() == null) menuItem.setDescription("");
            if (menuItem.getImageUrl() == null) menuItem.setImageUrl("");
            // ----------------------------------------------

            return ResponseEntity.ok(menuItemRepository.save(menuItem));
        } else {
            return ResponseEntity.badRequest().body("Nhà hàng không tồn tại với ID: " + resId);
        }
    }
    // 3. Xóa món ăn
    @DeleteMapping("/admin/menu/{id}")
    public ResponseEntity<?> deleteMenuItem(@PathVariable Long id) {
        if (!menuItemRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        menuItemRepository.deleteById(id);
        return ResponseEntity.ok("Đã xóa món ăn!");
    }
    
    // 4. Sửa món ăn
    @PutMapping("/admin/menu/{id}")
    public ResponseEntity<?> updateMenuItem(@PathVariable Long id, @RequestBody MenuItem req) {
        return menuItemRepository.findById(id).map(item -> {
            item.setName(req.getName());
            item.setPrice(req.getPrice());
            item.setDescription(req.getDescription());
            item.setImageUrl(req.getImageUrl());
            return ResponseEntity.ok(menuItemRepository.save(item));
        }).orElse(ResponseEntity.notFound().build());
    }
}