package com.s2o.backend_api.controller;

import com.s2o.backend_api.dto.ReviewRequest;
import com.s2o.backend_api.entity.Review;
import com.s2o.backend_api.entity.User;
import com.s2o.backend_api.repository.ReviewRepository;
import com.s2o.backend_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*")
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private UserRepository userRepository; // Để lấy tên người dùng

    // 1. GỬI ĐÁNH GIÁ
    @PostMapping("/add")
    public ResponseEntity<?> addReview(@RequestBody ReviewRequest req) {
        Review review = new Review();
        review.setUserId(req.getUserId());
        review.setMenuItemId(req.getMenuItemId());
        review.setRating(req.getRating());
        review.setComment(req.getComment());
        
        reviewRepository.save(review);
        return ResponseEntity.ok("Đánh giá thành công!");
    }

    // 2. XEM ĐÁNH GIÁ CỦA MÓN ĂN
    @GetMapping("/{menuItemId}")
    public List<Review> getReviews(@PathVariable Long menuItemId) {
        List<Review> reviews = reviewRepository.findByMenuItemIdOrderByCreatedAtDesc(menuItemId);
        
        // Điền tên người dùng vào (Logic đơn giản)
        for (Review r : reviews) {
            User u = userRepository.findById(r.getUserId()).orElse(null);
            if (u != null) {
                r.setUserName(u.getFullName() != null ? u.getFullName() : u.getUsername());
            } else {
                r.setUserName("Người dùng ẩn danh");
            }
        }
        return reviews;
    }
}