package com.s2o.backend_api.controller;

import com.s2o.backend_api.dto.LoginRequest;
import com.s2o.backend_api.dto.RegisterRequest;
import com.s2o.backend_api.entity.Restaurant;
import com.s2o.backend_api.entity.User;
import com.s2o.backend_api.repository.RestaurantRepository;
import com.s2o.backend_api.repository.UserRepository;
import com.s2o.backend_api.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RestaurantRepository restaurantRepository;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private PasswordEncoder passwordEncoder;

    // --- 1. ĐĂNG KÝ ---
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("Lỗi: Tên đăng nhập đã tồn tại!");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());

        // LOGIC PHÂN QUYỀN
        if ("MANAGER".equals(request.getRole())) {
            // Tạo nhà hàng mới
            if (request.getRestaurantName() != null) {
                Restaurant restaurant = new Restaurant();
                restaurant.setName(request.getRestaurantName());
                restaurant.setAddress(request.getAddress());
                
                // --- SỬA: Đặt PENDING để chờ Admin tối cao duyệt ---
                restaurant.setStatus("PENDING"); 
                // --------------------------------------------------
                
                Restaurant savedRes = restaurantRepository.save(restaurant);
                user.setRestaurantId(savedRes.getId());
                
                // --- SỬA: Tài khoản chủ quán cũng PENDING luôn ---
                user.setStatus("PENDING"); 
                // -------------------------------------------------
            }
        } 
        else if ("KITCHEN".equals(request.getRole())) {
            // Đầu bếp xin vào quán
            if (request.getRestaurantId() != null) {
                user.setRestaurantId(request.getRestaurantId());
                
                // QUAN TRỌNG: Đầu bếp mặc định PENDING (Chờ Chủ quán duyệt)
                user.setStatus("PENDING"); 
            } else {
                return ResponseEntity.badRequest().body("Đầu bếp cần nhập ID Nhà hàng!");
            }
        }
        else if ("STAFF".equals(request.getRole())) {
            // Nhân viên phục vụ xin vào quán
            if (request.getRestaurantId() != null) {
                user.setRestaurantId(request.getRestaurantId());
                // Mặc định PENDING chờ Manager duyệt
                user.setStatus("PENDING");
            } else {
                return ResponseEntity.badRequest().body("Nhân viên cần nhập ID Nhà hàng!");
            }
        }
        else {
            // Khách hàng
            user.setRole("USER");
            user.setStatus("ACTIVE");
        }

        userRepository.save(user);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Đăng ký thành công! Vui lòng chờ duyệt.");
        return ResponseEntity.ok(response);
    }

    // --- 2. ĐĂNG NHẬP ---
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest request) {
        try {
            // 1. Kiểm tra username/password
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            User user = userRepository.findByUsername(request.getUsername()).get();

            // 2. CHECK TRẠNG THÁI USER (Cho Đầu bếp & User thường & Manager)
            if ("PENDING".equals(user.getStatus())) {
                return ResponseEntity.badRequest().body("Tài khoản đang chờ duyệt! Vui lòng liên hệ quản trị viên.");
            }
            if ("BANNED".equals(user.getStatus())) {
                return ResponseEntity.badRequest().body("Tài khoản đã bị khóa!");
            }

            // 3. CHECK TRẠNG THÁI NHÀ HÀNG (Chỉ dành cho Manager)
            if ("MANAGER".equals(user.getRole()) && user.getRestaurantId() != null) {
                Optional<Restaurant> resOpt = restaurantRepository.findById(user.getRestaurantId());
                if (resOpt.isPresent()) {
                    String status = resOpt.get().getStatus();
                    if ("PENDING".equalsIgnoreCase(status)) return ResponseEntity.badRequest().body("Nhà hàng đang chờ duyệt!");
                    if ("REJECTED".equalsIgnoreCase(status)) return ResponseEntity.badRequest().body("Nhà hàng đã bị từ chối!");
                }
            }

            // 4. Cấp Token
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtils.generateToken(userDetails);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("type", "Bearer");
            response.put("id", user.getId());
            response.put("username", user.getUsername());
            response.put("fullName", user.getFullName());
            response.put("role", user.getRole());
            
            if (user.getRestaurantId() != null) {
                response.put("restaurantId", user.getRestaurantId());
                response.put("tenantId", user.getRestaurantId());
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Sai tài khoản hoặc mật khẩu!");
        }
    }
}