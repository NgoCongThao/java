package com.s2o.backend_api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "menu_items") // Phải khớp tên bảng trong SQL
@Data
public class MenuItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String name;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    private Double price;

    // Trong SQL là image_url, trong Java đặt là imageUrl cho chuẩn camelCase
    @Column(name = "image_url") 
    private String imageUrl;

    @Column(name = "is_available")
    private Boolean isAvailable = true;

    // Cột phân loại (Món chính, Trà sữa...)
    @Column(columnDefinition = "NVARCHAR(100)")
    private String category;

    // Liên kết với Nhà hàng
    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    @JsonIgnore // Quan trọng: Ngắt vòng lặp vô tận khi chuyển thành JSON
    private Restaurant restaurant;
}