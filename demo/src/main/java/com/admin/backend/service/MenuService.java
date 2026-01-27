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
        System.out.println(" tenantId trong service = " + tenantId);
        return menuRepository.findByTenantId(tenantId);
    }

    public Menu create(Menu menu, Long tenantId) {
        menu.setTenantId(tenantId);
        return menuRepository.save(menu);
    }

    public Menu update(Integer id, Menu newData, Long tenantId) {
        Menu menu = menuRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Menu not found"));

        if (!menu.getTenantId().equals(tenantId)) {
            throw new RuntimeException("Access denied");
        }

        menu.setName(newData.getName());
        menu.setDescription(newData.getDescription());
        menu.setPrice(newData.getPrice());
        menu.setCategory(newData.getCategory());
        menu.setAvailable(newData.getAvailable());

        return menuRepository.save(menu);
    }

    public void delete(Integer id, Long tenantId) {
        Menu menu = menuRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Menu not found"));

        if (!menu.getTenantId().equals(tenantId)) {
            throw new RuntimeException("Access denied");
        }

        menuRepository.delete(menu);
    }
}
