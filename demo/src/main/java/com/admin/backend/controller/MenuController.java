package com.admin.backend.controller;

import com.admin.backend.entity.Menu;
import com.admin.backend.service.MenuService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/menu")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping
    public List<Menu> getMenus(HttpServletRequest req) {
        Long tenantId = (Long) req.getAttribute("tenantId");
        return menuService.getMenus(tenantId);
    }

    @PostMapping
    public Menu create(@RequestBody Menu menu, HttpServletRequest req) {
        Long tenantId = (Long) req.getAttribute("tenantId");
        return menuService.create(menu, tenantId);
    }
}