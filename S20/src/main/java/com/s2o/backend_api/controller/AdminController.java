package com.s2o.backend_api.controller;

import com.s2o.backend_api.entity.MenuItem;
import com.s2o.backend_api.entity.Restaurant;
import com.s2o.backend_api.entity.User;
import com.s2o.backend_api.repository.MenuItemRepository;
import com.s2o.backend_api.repository.RestaurantRepository;
import com.s2o.backend_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RestaurantRepository restaurantRepository;
    
    // 👇 MỚI THÊM: Cần cái này để xóa món ăn trước khi xóa quán
    @Autowired
    private MenuItemRepository menuItemRepository;

    // 1. LẤY DANH SÁCH CHỜ DUYỆT (PENDING)
    @GetMapping("/pending-restaurants")
    public List<Map<String, Object>> getPendingRestaurants() {
        List<Restaurant> pendingRes = restaurantRepository.findAll().stream()
                .filter(r -> "PENDING".equalsIgnoreCase(r.getStatus()))
                .collect(Collectors.toList());

        return pendingRes.stream().map(res -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", res.getId());
            map.put("name", res.getName());
            map.put("address", res.getAddress());
            map.put("time", res.getTime());
            
            Optional<User> owner = userRepository.findAll().stream()
                    .filter(u -> "MANAGER".equals(u.getRole()) 
                            && u.getRestaurantId() != null 
                            && u.getRestaurantId().equals(res.getId()))
                    .findFirst();
            
            map.put("ownerName", owner.map(User::getFullName).orElse("Không rõ"));
            map.put("ownerPhone", owner.map(User::getPhone).orElse("---"));
            map.put("ownerUsername", owner.map(User::getUsername).orElse("---"));
            
            return map;
        }).collect(Collectors.toList());
    }

    // 2. DUYỆT (APPROVE) -> KÍCH HOẠT CẢ QUÁN VÀ CHỦ
    @PostMapping("/approve/{resId}")
    public ResponseEntity<?> approveRestaurant(@PathVariable Long resId) {
        Optional<Restaurant> resOpt = restaurantRepository.findById(resId);
        if (resOpt.isEmpty()) return ResponseEntity.notFound().build();

        // A. Kích hoạt Quán
        Restaurant res = resOpt.get();
        res.setStatus("ACTIVE"); 
        restaurantRepository.save(res);
        
        // B. Kích hoạt Tài khoản Chủ quán
        List<User> managers = userRepository.findAll().stream()
                .filter(u -> "MANAGER".equals(u.getRole()) 
                        && u.getRestaurantId() != null 
                        && u.getRestaurantId().equals(resId))
                .collect(Collectors.toList());

        for (User u : managers) {
            u.setStatus("ACTIVE");
            userRepository.save(u);
        }

        return ResponseEntity.ok("Đã duyệt nhà hàng và kích hoạt tài khoản chủ quán!");
    }

    // 3. TỪ CHỐI (REJECT) -> XÓA SẠCH SẼ (FIX LỖI KHÔNG XÓA ĐƯỢC)
    @PostMapping("/reject/{resId}")
    public ResponseEntity<?> rejectRestaurant(@PathVariable Long resId) {
        try {
            // BƯỚC 1: Xóa tất cả Món ăn của quán này trước (Tránh lỗi khóa ngoại)
            List<MenuItem> menuItems = menuItemRepository.findByRestaurantId(resId);
            menuItemRepository.deleteAll(menuItems);

            // BƯỚC 2: Xóa User chủ quán
            List<User> owners = userRepository.findAll().stream()
                    .filter(u -> "MANAGER".equals(u.getRole()) 
                            && u.getRestaurantId() != null 
                            && u.getRestaurantId().equals(resId))
                    .collect(Collectors.toList());
            userRepository.deleteAll(owners);
            
            // BƯỚC 3: Xóa nhà hàng
            restaurantRepository.deleteById(resId);
            
            return ResponseEntity.ok("Đã từ chối và xóa yêu cầu đăng ký!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi xóa: " + e.getMessage());
        }
    }

    // --- CÁC API QUẢN LÝ KHÁC (GIỮ NGUYÊN) ---
    @GetMapping("/users")
    public List<User> getAllUsers() { return userRepository.findAll(); }

    @GetMapping("/restaurants")
    public List<Restaurant> getAllRestaurants() { return restaurantRepository.findAll(); }

    @PutMapping("/restaurants/{id}")
    public ResponseEntity<?> updateRestaurant(@PathVariable Long id, @RequestBody Restaurant resDetails) {
        Optional<Restaurant> resOpt = restaurantRepository.findById(id);
        if (resOpt.isEmpty()) return ResponseEntity.notFound().build();
        Restaurant res = resOpt.get();
        res.setName(resDetails.getName());
        res.setAddress(resDetails.getAddress());
        res.setPhone(resDetails.getPhone());
        res.setTime(resDetails.getTime());
        res.setDescription(resDetails.getDescription());
        res.setTotalTables(resDetails.getTotalTables());
        res.setRating(resDetails.getRating());
        res.setCategory(resDetails.getCategory());
        res.setImage(resDetails.getImage());
        res.setIsOpen(resDetails.getIsOpen());
        res.setStatus(resDetails.getStatus());
        restaurantRepository.save(res);
        return ResponseEntity.ok("Cập nhật thành công!");
    }
    
    @DeleteMapping("/restaurants/{id}")
    public ResponseEntity<?> deleteRestaurant(@PathVariable Long id) {
        try { restaurantRepository.deleteById(id); return ResponseEntity.ok("Đã xóa!"); }
        catch (Exception e) { return ResponseEntity.badRequest().body("Lỗi xóa!"); }
    }
    
    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();
        User user = userOpt.get();
        user.setFullName(userDetails.getFullName());
        user.setRole(userDetails.getRole());
        user.setRestaurantId(userDetails.getRestaurantId());
        userRepository.save(user);
        return ResponseEntity.ok("Cập nhật User thành công!");
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok("Đã xóa User!");
    }
}