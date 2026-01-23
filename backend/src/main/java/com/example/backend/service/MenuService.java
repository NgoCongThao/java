package com.example.backend.service;

import com.example.backend.entity.Menu;
import com.example.backend.repository.MenuRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MenuService {

    @Autowired
    private MenuRepository menuRepository;

    public List<Menu> getAll() {
        return menuRepository.findAll();
    }

    public Menu create(Menu menu) {
        return menuRepository.save(menu);
    }

    public Menu update(Long id, Menu menu) {
        Menu existing = menuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu không tồn tại"));

        existing.setName(menu.getName());
        existing.setPrice(menu.getPrice());

        return menuRepository.save(existing);
    }

    public void delete(Long id) {
        menuRepository.deleteById(id);
    }
}
