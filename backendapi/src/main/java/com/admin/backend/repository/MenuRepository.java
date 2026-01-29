package com.admin.backend.repository;

import com.admin.backend.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Integer> {
    List<Menu> findByTenantId(Long tenantId);
}