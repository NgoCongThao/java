package com.admin.backend.controller;

import com.admin.backend.entity.Menu;
import com.admin.backend.service.MenuService;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/menu")
//@PreAuthorize("hasRole('MANAGER')")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

   @GetMapping
public List<Menu> getMenus(HttpServletRequest req) {
    Long tenantId = (Long) req.getAttribute("tenantId");
    System.out.println("🔥 tenantId trong Controller = " + tenantId);
System.out.println("🔥 tenantId class = " + (tenantId != null ? tenantId.getClass() : "null"));
    return menuService.getMenus(tenantId);
}

   @PostMapping
public Menu create(@RequestBody Menu menu, HttpServletRequest req) {
    Long tenantId = (Long) req.getAttribute("tenantId");
    return menuService.create(menu, tenantId); // ✅ ĐÚNG
}

@PutMapping("/{id}")
public Menu update(
        @PathVariable Integer id,
        @RequestBody Menu menu,
        HttpServletRequest req
) {
    Long tenantId = (Long) req.getAttribute("tenantId");
    return menuService.update(id, menu, tenantId);
}

@DeleteMapping("/{id}")
public void delete(
        @PathVariable Integer id,
        HttpServletRequest req
) {
    Long tenantId = (Long) req.getAttribute("tenantId");
    menuService.delete(id, tenantId);
}

}