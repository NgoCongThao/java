package com.s2o.backend.repository;

import com.s2o.backend.entity.DiningTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiningTableRepository extends JpaRepository<DiningTable, Long> {
    // Tìm tất cả bàn của một nhà hàng (Sau này dùng khi mở rộng chuỗi)
    List<DiningTable> findByRestaurantId(Long restaurantId);
}