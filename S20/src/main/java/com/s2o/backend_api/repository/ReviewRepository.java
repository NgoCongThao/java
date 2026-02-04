package com.s2o.backend_api.repository;

import com.s2o.backend_api.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    // Lấy tất cả đánh giá của 1 món ăn, sắp xếp mới nhất lên đầu
    List<Review> findByMenuItemIdOrderByCreatedAtDesc(Long menuItemId);
}