package com.s2o.backend_api.repository;

import com.s2o.backend_api.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    // Hiện tại chưa cần hàm tìm kiếm gì đặc biệt, cứ để trống là dùng được hàm findAll()
}