package com.s2o.backend_api.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // Nhớ import cái này
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "menu_items")
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

    @Column(name = "image_url") 
    private String imageUrl;

    @Column(name = "is_available")
    private Boolean isAvailable = true;

    @Column(columnDefinition = "NVARCHAR(100)")
    private String category;

    // --- SỬA ĐOẠN NÀY ---
    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    // Thay @JsonIgnore bằng dòng dưới đây:
    // Ý nghĩa: Cho phép đọc/ghi object Restaurant, nhưng KHÔNG đi sâu vào list menuItems của nó
    @JsonIgnoreProperties({"menuItems", "user", "bookings", "hibernateLazyInitializer", "handler"}) 
    private Restaurant restaurant;
}