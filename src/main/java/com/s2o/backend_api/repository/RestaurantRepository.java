package com.s2o.backend_api.repository;

import com.s2o.backend_api.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    // Không cần thêm method nào ở đây (chỉ dùng findById mặc định của JpaRepository)
}