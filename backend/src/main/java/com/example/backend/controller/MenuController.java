package com.example.backend.controller;

import com.example.backend.entity.Menu;
import com.example.backend.service.MenuService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menus")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping
    public List<Menu> getAll() {
        return menuService.getAll();
    }

    @PostMapping
    public Menu create(@RequestBody Menu menu) {
        return menuService.create(menu);
    }

    @PutMapping("/{id}")
    public Menu update(@PathVariable Long id, @RequestBody Menu menu) {
        return menuService.update(id, menu);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        menuService.delete(id);
    }
}
