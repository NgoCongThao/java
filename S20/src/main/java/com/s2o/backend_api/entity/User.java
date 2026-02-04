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
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "full_name", columnDefinition = "NVARCHAR(255)")
    private String fullName;
   
    private String phone;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String address;

    private String role; // "USER" hoặc "KITCHEN"

    // THÊM TRƯỜNG NÀY
    @Column(name = "restaurant_id")
    private Long restaurantId;
}