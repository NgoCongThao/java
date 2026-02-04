package com.s2o.backend_api.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.s2o.backend_api.entity.MenuItem;
import com.s2o.backend_api.entity.Restaurant;
import com.s2o.backend_api.repository.MenuItemRepository;
import com.s2o.backend_api.repository.RestaurantRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

 private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final com.s2o.backend_api.repository.UserRepository userRepository; // Thêm dòng này
    private final ObjectMapper objectMapper;

   @Override
    public void run(String... args) throws Exception {
        createSuperAdmin(); // Thêm dòng này chạy trước
        loadRestaurantData();
        loadMenuData();
    }

    private void createSuperAdmin() {
        // Tìm xem user 'admin' có chưa, nếu chưa thì tạo mới
        com.s2o.backend_api.entity.User admin = userRepository.findByUsername("admin")
                .orElse(new com.s2o.backend_api.entity.User());

        // Cập nhật lại thông tin (để đảm bảo luôn đúng quyền)
        admin.setUsername("admin");
        if (admin.getPassword() == null) {
             admin.setPassword("admin123"); // Chỉ set pass nếu là user mới
        }
        admin.setFullName("Super Administrator");
        
        // --- QUAN TRỌNG NHẤT: ÉP VỀ ADMIN ---
        admin.setRole("ADMIN"); 
        
        userRepository.save(admin);
        System.out.println("Seed Data: Đã cập nhật quyền ADMIN cho tài khoản 'admin'");
    }
    private void loadRestaurantData() {
        if (restaurantRepository.count() == 0) {
            try {
                InputStream inputStream = TypeReference.class.getResourceAsStream("/data/restaurants.json");
                if (inputStream == null) {
                    System.out.println("Seed Data: Không tìm thấy file restaurants.json!");
                    return;
                }
                List<Restaurant> restaurants = objectMapper.readValue(inputStream, new TypeReference<List<Restaurant>>() {});
                restaurantRepository.saveAll(restaurants);
                System.out.println("Seed Data: Đã nạp thành công " + restaurants.size() + " nhà hàng.");
            } catch (IOException e) {
                System.out.println("Seed Data: Lỗi đọc file nhà hàng: " + e.getMessage());
            }
        }
    }

    private void loadMenuData() {
        if (menuItemRepository.count() == 0 && restaurantRepository.count() > 0) {
            try {
                InputStream inputStream = TypeReference.class.getResourceAsStream("/data/menus.json");
                if (inputStream == null) {
                    System.out.println("Seed Data: Không tìm thấy file menus.json!");
                    return;
                }
                List<MenuImportDTO> menuImports = objectMapper.readValue(inputStream, new TypeReference<List<MenuImportDTO>>() {});
                List<MenuItem> allMenuItems = new ArrayList<>();
                List<Restaurant> savedRestaurants = restaurantRepository.findAll();
                
                // Map ID JSON sang Restaurant Entity
                Map<Long, Restaurant> restaurantMap = new HashMap<>();
                // Giả định ID trong DB tự tăng khớp với thứ tự trong JSON (vì DB đang trống)
                long jsonIdCounter = 1; 
                for (Restaurant r : savedRestaurants) {
                    restaurantMap.put(jsonIdCounter++, r);
                }

                for (MenuImportDTO importData : menuImports) {
                    Restaurant restaurant = restaurantMap.get(importData.getRestaurantId());
                    if (restaurant != null) {
                        for (MenuCategoryDTO catDTO : importData.getMenu()) {
                            for (MenuItemJsonDTO itemDTO : catDTO.getItems()) {
                                MenuItem menuItem = new MenuItem();
                                menuItem.setName(itemDTO.getName());
                                menuItem.setPrice(itemDTO.getPrice());
                                menuItem.setDescription(itemDTO.getDescription());
                                menuItem.setImageUrl(itemDTO.getImage());
                                menuItem.setCategory(catDTO.getCategory());
                                menuItem.setRestaurant(restaurant);
                                menuItem.setIsAvailable(true);
                                allMenuItems.add(menuItem);
                            }
                        }
                    }
                }
                menuItemRepository.saveAll(allMenuItems);
                System.out.println("Seed Data: Đã nạp thành công " + allMenuItems.size() + " món ăn.");
            } catch (IOException e) {
                System.out.println("Seed Data: Lỗi đọc file menu: " + e.getMessage());
            }
        }
    }

    // Các class phụ để hứng dữ liệu JSON
    @Data
    static class MenuImportDTO {
        private Long restaurantId;
        private String restaurantName;
        private List<MenuCategoryDTO> menu;
    }

    @Data
    static class MenuCategoryDTO {
        private String category;
        private List<MenuItemJsonDTO> items;
    }

    @Data
    static class MenuItemJsonDTO {
        private Long id;
        private String name;
        private Double price;
        private String image;
        private String description;
    }
}