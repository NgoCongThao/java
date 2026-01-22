package com.s2o.backend.controller.staff;

import com.s2o.backend.entity.Product;
import com.s2o.backend.entity.User;
import com.s2o.backend.repository.ProductRepository;
import com.s2o.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff/products")
@CrossOrigin(origins = "http://localhost:3000")
public class StaffProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository; // <--- THÊM REPO NÀY ĐỂ TÌM USER

    // --- HÀM PHỤ: Lấy ID nhà hàng từ Token ---
    private Long getCurrentRestaurantId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại!"));
        return user.getRestaurant().getId();
    }

    @GetMapping
    public List<Product> getMenu() {
        // KHÔNG DÙNG 1L NỮA -> Gọi hàm lấy động
        Long restaurantId = getCurrentRestaurantId();

        // Trả về danh sách món ăn của đúng nhà hàng đó
        return productRepository.findByRestaurantId(restaurantId);
    }
}