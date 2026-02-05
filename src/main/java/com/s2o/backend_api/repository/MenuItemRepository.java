package com.s2o.backend_api.repository;

import com.s2o.backend_api.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    
    // Tìm tất cả món ăn của một nhà hàng cụ thể
    List<MenuItem> findByRestaurantId(Long restaurantId);
}