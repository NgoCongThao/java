package com.s2o.backend_api.controller;
import org.springframework.http.ResponseEntity;
import com.s2o.backend_api.repository.RestaurantRepository;
import com.s2o.backend_api.entity.MenuItem;
import com.s2o.backend_api.repository.MenuItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/guest/menu") // Đường dẫn chung
@CrossOrigin(origins = "*")        // Quan trọng: Để HTML gọi được mà không bị chặn
public class MenuController {

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    // API: Lấy thực đơn của nhà hàng theo ID
    // Link gọi: GET http://localhost:8080/api/guest/menu/restaurant/1
    @GetMapping("/restaurant/{restaurantId}")
    public List<MenuItem> getMenuByRestaurant(@PathVariable Long restaurantId) {
        // Trả về danh sách phẳng (Flat List). 
        // Việc gom nhóm (Category) đã được file menu.html xử lý bằng Javascript rồi.
        return menuItemRepository.findByRestaurantId(restaurantId);
    }
    // --- ADMIN API ---

    // 1. Thêm món ăn vào nhà hàng
    @PostMapping("/admin/menu")
    public ResponseEntity<?> addMenuItem(@RequestBody MenuItem menuItem) {
        if (menuItem.getRestaurant() == null || menuItem.getRestaurant().getId() == null) {
            return ResponseEntity.badRequest().body("Phải cung cấp ID nhà hàng");
        }

        // Sửa đoạn này: Dùng if/else thay vì map().orElse() để tránh lỗi Type Inference
        var restaurantOpt = restaurantRepository.findById(menuItem.getRestaurant().getId());
        
        if (restaurantOpt.isPresent()) {
            menuItem.setRestaurant(restaurantOpt.get());
            return ResponseEntity.ok(menuItemRepository.save(menuItem));
        } else {
            return ResponseEntity.badRequest().body("Nhà hàng không tồn tại");
        }
    }

    // 2. Xóa món ăn
    @DeleteMapping("/admin/menu/{id}")
    public ResponseEntity<?> deleteMenuItem(@PathVariable Long id) {
        if (!menuItemRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        menuItemRepository.deleteById(id);
        return ResponseEntity.ok("Đã xóa món ăn!");
    }
    
    // 3. Sửa món ăn (Giá, Tên...)
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