package com.s2o.backend_api.controller;

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

    // API: Lấy thực đơn của nhà hàng theo ID
    // Link gọi: GET http://localhost:8080/api/guest/menu/restaurant/1
    @GetMapping("/restaurant/{restaurantId}")
    public List<MenuItem> getMenuByRestaurant(@PathVariable Long restaurantId) {
        // Trả về danh sách phẳng (Flat List). 
        // Việc gom nhóm (Category) đã được file menu.html xử lý bằng Javascript rồi.
        return menuItemRepository.findByRestaurantId(restaurantId);
    }
}