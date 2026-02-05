package com.s2o.backend_api.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String fullName;
    private String phone;

    // Dành cho Bếp (KITCHEN) hoặc nhập tay ID quán
    private Long restaurantId;

    // --- 👇 CÁC TRƯỜNG MỚI CHO CHỦ NHÀ HÀNG (MANAGER) 👇 ---
    
    // Để phân biệt vai trò: "MANAGER", "USER", "KITCHEN"
    private String role;           
    
    // Tên quán (Bắt buộc nếu là MANAGER)
    private String restaurantName; 
    
    // Địa chỉ quán (Bắt buộc nếu là MANAGER)
    private String address;        
}