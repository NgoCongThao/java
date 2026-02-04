package com.s2o.backend_api.controller;

import com.s2o.backend_api.dto.ReviewRequest;
import com.s2o.backend_api.entity.MenuItem;
import com.s2o.backend_api.entity.Review;
import com.s2o.backend_api.entity.User;
import com.s2o.backend_api.repository.MenuItemRepository;
import com.s2o.backend_api.repository.ReviewRepository;
import com.s2o.backend_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*")
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private UserRepository userRepository; // Cần cái này để tìm User từ ID
    
    @Autowired
    private MenuItemRepository menuItemRepository; // Cần cái này để tìm Món ăn từ ID

    // 1. API: Lấy danh sách đánh giá của 1 món ăn
    // URL: GET /api/reviews/{itemId}
    @GetMapping("/{itemId}")
    public ResponseEntity<?> getReviews(@PathVariable Long itemId) {
        // Lấy list review từ DB
        List<Review> reviews = reviewRepository.findByMenuItemIdOrderByCreatedAtDesc(itemId);
        
        // Chuyển đổi sang JSON đẹp để trả về Frontend
        List<Map<String, Object>> response = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (Review r : reviews) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            
            // Lấy tên thật từ bảng User
            String fullName = r.getUser().getFullName();
            if (fullName == null) fullName = r.getUser().getUsername();
            map.put("user", fullName); 

            // Tạo Avatar giả (Lấy chữ cái đầu)
            String avatar = "U";
            if (fullName != null && !fullName.isEmpty()) {
                String[] names = fullName.trim().split(" ");
                avatar = names[names.length - 1].substring(0, 1).toUpperCase();
            }
            map.put("avatar", avatar);
            
            map.put("rate", r.getRating());
            map.put("comment", r.getComment());
            map.put("date", r.getCreatedAt().format(formatter));
            
            response.add(map);
        }
        return ResponseEntity.ok(response);
    }

    // 2. API: Gửi đánh giá mới
    // URL: POST /api/reviews/add
    @PostMapping("/add")
    public ResponseEntity<?> addReview(@RequestBody ReviewRequest request) {
        // Tìm User trong DB
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
        
        // Tìm Món ăn trong DB
        MenuItem item = menuItemRepository.findById(request.getItemId())
                .orElseThrow(() -> new RuntimeException("Món ăn không tồn tại"));

        // --- (MỚI) VALIDATE SỐ SAO (Chặn Postman hack) ---
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            return ResponseEntity.badRequest().body("Lỗi: Số sao đánh giá phải từ 1 đến 5!");
        }
        // Tạo Review mới và lưu
        Review review = new Review();
        review.setUser(user);       // Gán User thật vào
        review.setMenuItem(item);   // Gán Món ăn thật vào
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        reviewRepository.save(review);

        return ResponseEntity.ok("Đánh giá thành công!");
    }
}