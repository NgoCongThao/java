package com.s2o.backend_api.controller;

import com.s2o.backend_api.dto.RestaurantUpdateRequest;
import com.s2o.backend_api.entity.MenuItem;
import com.s2o.backend_api.entity.Restaurant;
import com.s2o.backend_api.entity.User;
import com.s2o.backend_api.repository.MenuItemRepository;
import com.s2o.backend_api.repository.RestaurantRepository;
import com.s2o.backend_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/partner")
@CrossOrigin(origins = "*")
public class PartnerController {

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    // --- HÀM TIỆN ÍCH: Lấy ID Nhà Hàng ---
    private Long getCurrentRestaurantId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName(); 
        User user = userRepository.findByUsername(username).orElse(null);
        
        if (user != null && "MANAGER".equals(user.getRole())) {
            return user.getRestaurantId(); 
        }
        return null;
    }

    // ==========================================
    // PHẦN 1: QUẢN LÝ MENU
    // ==========================================

    @GetMapping("/menu")
    public ResponseEntity<?> getMyMenu() {
        Long resId = getCurrentRestaurantId();
        if (resId == null) return ResponseEntity.status(403).body("Bạn không có quyền quản lý nhà hàng!");

        List<MenuItem> items = menuItemRepository.findByRestaurantId(resId);
        return ResponseEntity.ok(items);
    }

    @PostMapping("/menu")
    public ResponseEntity<?> addMenuItem(@RequestBody MenuItem item) {
        Long resId = getCurrentRestaurantId();
        if (resId == null) return ResponseEntity.status(403).body("Lỗi quyền hạn!");

        // Tạo đối tượng Restaurant giả để gán ID
        Restaurant res = new Restaurant();
        res.setId(resId); 
        
        item.setRestaurant(res);
        
        if (item.getIsAvailable() == null) {
            item.setIsAvailable(true); 
        }
        
        menuItemRepository.save(item);
        return ResponseEntity.ok("Thêm món thành công!");
    }

    @DeleteMapping("/menu/{id}")
    public ResponseEntity<?> deleteMenuItem(@PathVariable Long id) {
        Long resId = getCurrentRestaurantId();
        if (resId == null) return ResponseEntity.status(403).body("Lỗi quyền hạn!");
        
        MenuItem item = menuItemRepository.findById(id).orElse(null);
        
        if (item == null || !item.getRestaurant().getId().equals(resId)) {
            return ResponseEntity.badRequest().body("Món không tồn tại hoặc không thuộc quán bạn!");
        }

        menuItemRepository.deleteById(id);
        return ResponseEntity.ok("Đã xóa món!");
    }

    // ==========================================
    // PHẦN 2: THÔNG TIN NHÀ HÀNG
    // ==========================================

    @GetMapping("/info")
    public ResponseEntity<?> getMyRestaurantInfo() {
        Long resId = getCurrentRestaurantId();
        if (resId == null) return ResponseEntity.status(403).body("Không có quyền!");

        Restaurant restaurant = restaurantRepository.findById(resId).orElse(null);
        return ResponseEntity.ok(restaurant);
    }

    @PutMapping("/info")
    public ResponseEntity<?> updateMyRestaurantInfo(@RequestBody RestaurantUpdateRequest req) {
        Long resId = getCurrentRestaurantId();
        if (resId == null) return ResponseEntity.status(403).body("Không có quyền!");

        Restaurant restaurant = restaurantRepository.findById(resId).orElse(null);
        if (restaurant == null) return ResponseEntity.badRequest().body("Không tìm thấy quán!");

        // --- CẬP NHẬT FULL CÁC TRƯỜNG KHỚP VỚI ENTITY RESTAURANT CỦA BẠN ---
        if (req.getName() != null) restaurant.setName(req.getName());
        if (req.getAddress() != null) restaurant.setAddress(req.getAddress());
        if (req.getPhone() != null) restaurant.setPhone(req.getPhone());
        if (req.getDescription() != null) restaurant.setDescription(req.getDescription());
        if (req.getImage() != null) restaurant.setImage(req.getImage());
        if (req.getTime() != null) restaurant.setTime(req.getTime());
        if (req.getCategory() != null) restaurant.setCategory(req.getCategory());
        if (req.getTotalTables() != null) restaurant.setTotalTables(req.getTotalTables());
        if (req.getIsOpen() != null) restaurant.setIsOpen(req.getIsOpen());
        
        // Cập nhật tọa độ nếu có
        if (req.getLatitude() != null) restaurant.setLatitude(req.getLatitude());
        if (req.getLongitude() != null) restaurant.setLongitude(req.getLongitude());

        restaurantRepository.save(restaurant);
        return ResponseEntity.ok("Cập nhật thành công!");
    }

    @PutMapping("/menu/{id}")
    public ResponseEntity<?> updateMenuItem(@PathVariable Long id, @RequestBody MenuItem req) {
        Long resId = getCurrentRestaurantId();
        if (resId == null) return ResponseEntity.status(403).body("Lỗi quyền hạn!");

        // 1. Tìm món ăn trong DB
        MenuItem item = menuItemRepository.findById(id).orElse(null);
        
        // 2. Kiểm tra bảo mật: Món này có thuộc quán của bạn không?
        if (item == null || !item.getRestaurant().getId().equals(resId)) {
            return ResponseEntity.badRequest().body("Món không tồn tại hoặc không thuộc quán bạn!");
        }

        // 3. Cập nhật dữ liệu mới
        item.setName(req.getName());
        item.setPrice(req.getPrice());
        item.setDescription(req.getDescription());
        item.setImageUrl(req.getImageUrl());
        item.setCategory(req.getCategory());
        item.setIsAvailable(req.getIsAvailable());

        // 4. Lưu lại
        menuItemRepository.save(item);
        return ResponseEntity.ok("Cập nhật món thành công!");
    }

    // ==========================================
    // PHẦN 3: QUẢN LÝ NHÂN VIÊN (MỚI THÊM)
    // ==========================================

    // 1. Lấy danh sách nhân viên theo trạng thái (PENDING hoặc ACTIVE)
    @GetMapping("/staff")
    public ResponseEntity<?> getStaffList(@RequestParam String status) {
        Long resId = getCurrentRestaurantId();
        if (resId == null) return ResponseEntity.status(403).body("Lỗi quyền hạn!");

        // Gọi hàm tìm kiếm đã viết trong UserRepository
        List<User> staff = userRepository.findByRestaurantIdAndRoleAndStatus(resId, "KITCHEN", status);
        return ResponseEntity.ok(staff);
    }

    // 2. Duyệt nhân viên
    @PutMapping("/staff/{userId}/approve")
    public ResponseEntity<?> approveStaff(@PathVariable Long userId) {
        Long resId = getCurrentRestaurantId();
        User staff = userRepository.findById(userId).orElse(null);

        // Kiểm tra xem nhân viên này có đúng là xin vào quán mình không
        if (staff == null || !staff.getRestaurantId().equals(resId)) {
            return ResponseEntity.badRequest().body("Nhân viên không tồn tại hoặc không thuộc quán bạn!");
        }

        staff.setStatus("ACTIVE"); // Duyệt thành công
        userRepository.save(staff);
        return ResponseEntity.ok("Đã duyệt nhân viên!");
    }

    // 3. Xóa/Sa thải/Từ chối nhân viên
    @DeleteMapping("/staff/{userId}")
    public ResponseEntity<?> deleteStaff(@PathVariable Long userId) {
        Long resId = getCurrentRestaurantId();
        User staff = userRepository.findById(userId).orElse(null);

        // Kiểm tra bảo mật
        if (staff == null || !staff.getRestaurantId().equals(resId)) {
            return ResponseEntity.badRequest().body("Lỗi quyền hạn!");
        }

        userRepository.delete(staff);
        return ResponseEntity.ok("Đã xóa nhân viên!");
    }
}