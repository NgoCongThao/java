package com.example.backend.repository;

import com.example.backend.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    // KHÔNG CẦN GÌ THÊM
}
