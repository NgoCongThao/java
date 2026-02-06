package com.s2o.backend_api.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.s2o.backend_api.entity.MenuItem;
import com.s2o.backend_api.entity.Restaurant;
import com.s2o.backend_api.entity.User; // Import rõ ràng
import com.s2o.backend_api.repository.MenuItemRepository;
import com.s2o.backend_api.repository.RestaurantRepository;
import com.s2o.backend_api.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor // Tự động inject tất cả các biến 'final'
public class DataSeeder implements CommandLineRunner {

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final PasswordEncoder passwordEncoder; // Đã sửa thành final, bỏ @Autowired

    @Override
    public void run(String... args) throws Exception {
        System.out.println("----- STARTING DATA SEEDING -----");
        createSuperAdmin();
        loadRestaurantData();
        createRestaurantManagers();
        loadMenuData();
        System.out.println("----- DATA SEEDING FINISHED -----");
    }

    private void createSuperAdmin() {
        // Tìm user admin, nếu không có thì tạo mới
        User admin = userRepository.findByUsername("admin").orElse(new User());

        // Luôn set lại các thông tin này để đảm bảo đúng cấu hình
        admin.setUsername("admin");
        admin.setFullName("Super Administrator");
        admin.setRole("ADMIN");

        // SỬA LỖI: Luôn mã hóa lại mật khẩu để đảm bảo đăng nhập được
        // Dù DB cũ có lưu gì thì giờ nó cũng sẽ là chuỗi hash của "admin123"
        admin.setPassword(passwordEncoder.encode("admin123"));

        userRepository.save(admin);
        System.out.println("Seed Data: Admin user 'admin' / 'admin123' đã sẵn sàng.");
    }

    private void loadRestaurantData() {
        if (restaurantRepository.count() == 0) {
            try {
                InputStream inputStream = TypeReference.class.getResourceAsStream("/data/restaurants.json");
                if (inputStream == null) {
                    System.out.println("Seed Data: ⚠️ Không tìm thấy file /data/restaurants.json");
                    return;
                }
                List<Restaurant> restaurants = objectMapper.readValue(inputStream, new TypeReference<List<Restaurant>>() {});

                // Set default values nếu trong JSON thiếu
                restaurants.forEach(r -> {
                    if (r.getIsOpen() == null) r.setIsOpen(true);
                });

                restaurantRepository.saveAll(restaurants);
                System.out.println("Seed Data: Đã nạp " + restaurants.size() + " nhà hàng.");
            } catch (IOException e) {
                System.out.println("Seed Data: ❌ Lỗi nạp nhà hàng: " + e.getMessage());
            }
        }
    }
    private void createRestaurantManagers() {
        List<Restaurant> restaurants = restaurantRepository.findAll();

        if (restaurants.isEmpty()) return;

        int count = 0;
        for (Restaurant res : restaurants) {
            // Quy ước: User là "manager_{id}", Pass là "123456"
            String username = "manager_" + res.getId();

            if (userRepository.findByUsername(username).isEmpty()) {
                User manager = new User();
                manager.setUsername(username);
                manager.setPassword(passwordEncoder.encode("123456")); // Mật khẩu mặc định
                manager.setFullName("Quản lý - " + res.getName());
                manager.setRole("MANAGER");
                manager.setRestaurantId(res.getId()); // Gắn User này vào nhà hàng

                userRepository.save(manager);
                count++;
            }
        }
        if (count > 0) {
            System.out.println("Seed Data: Đã tạo tự động " + count + " tài khoản Manager (Pass: 123456)");
        }
    }
    private void loadMenuData() {
        if (menuItemRepository.count() == 0 && restaurantRepository.count() > 0) {
            try {
                InputStream inputStream = TypeReference.class.getResourceAsStream("/data/menus.json");
                if (inputStream == null) {
                    System.out.println("Seed Data: ⚠️ Không tìm thấy file /data/menus.json");
                    return;
                }
                List<MenuImportDTO> menuImports = objectMapper.readValue(inputStream, new TypeReference<List<MenuImportDTO>>() {});
                List<MenuItem> allMenuItems = new ArrayList<>();

                // Lấy tất cả nhà hàng lên
                List<Restaurant> savedRestaurants = restaurantRepository.findAll();

                // Map thông minh hơn: Dùng Name hoặc giả định index
                // Ở đây thầy giữ logic index của em nhưng thêm check an toàn
                Map<Long, Restaurant> restaurantMap = new HashMap<>();
                long jsonIdCounter = 1;
                for (Restaurant r : savedRestaurants) {
                    restaurantMap.put(jsonIdCounter++, r);
                }

                for (MenuImportDTO importData : menuImports) {
                    // Logic map ID này chỉ đúng khi DB mới tinh (Auto increment từ 1)
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
                                menuItem.setRestaurant(restaurant); // Link món ăn với nhà hàng
                                menuItem.setIsAvailable(true);
                                allMenuItems.add(menuItem);
                            }
                        }
                    } else {
                        System.out.println("Seed Data: ⚠️ Không tìm thấy nhà hàng khớp với ID JSON: " + importData.getRestaurantId());
                    }
                }
                menuItemRepository.saveAll(allMenuItems);
                System.out.println("Seed Data: Đã nạp " + allMenuItems.size() + " món ăn.");
            } catch (IOException e) {
                System.out.println("Seed Data: ❌ Lỗi nạp menu: " + e.getMessage());
            }
        }
    }

    // --- DTO CLASSES (Giữ nguyên) ---
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