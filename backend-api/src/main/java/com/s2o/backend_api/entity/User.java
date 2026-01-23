package com.s2o.backend_api.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username; // Tên đăng nhập (hoặc email)

    @Column(nullable = false)
    private String password; // Mật khẩu

    @Column(name = "full_name", columnDefinition = "NVARCHAR(255)") // Thêm dòng này
    private String fullName;
    
    private String phone;    // Số điện 
    
    @Column(columnDefinition = "NVARCHAR(255)") // Thêm dòng này
    private String address;  // Địa chỉ mặc định

    private String role;     // "USER" hoặc "ADMIN"
}