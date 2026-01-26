package com.admin.backend.service;

import com.admin.backend.entity.Menu;
import com.admin.backend.repository.MenuRepository;
import com.admin.backend.util.TenantContext;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MenuService {

    private final MenuRepository menuRepository;

    public MenuService(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    public List<Menu> getAll() {
        Long tenantId = TenantContext.getTenantId();
        return menuRepository.findByTenantId(tenantId);
    }

    public Menu create(Menu menu) {
        menu.setTenantId(TenantContext.getTenantId());
        return menuRepository.save(menu);
    }
}