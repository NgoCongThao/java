package com.admin.backend.service;

import com.admin.backend.entity.Menu;
import com.admin.backend.repository.MenuRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MenuService {

    private final MenuRepository menuRepository;

    public MenuService(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    public List<Menu> getMenus(Long tenantId) {
        return menuRepository.findByTenantId(tenantId);
    }

    public Menu create(Menu menu, Long tenantId) {
        menu.setTenantId(tenantId);
        return menuRepository.save(menu);
    }
}