package com.admin.backend.repository;

import com.admin.backend.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    List<Menu> findByTenantId(Long tenantId);

    Optional<Menu> findByIdAndTenantId(Long id, Long tenantId);

    void deleteByIdAndTenantId(Long id, Long tenantId);
}