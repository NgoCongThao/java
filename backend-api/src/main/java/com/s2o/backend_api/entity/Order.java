package com.s2o.backend_api.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "restaurant_name")
    private String restaurantName;

    @Column(name = "total_price")
    private Double totalPrice;

    private String status;
    private String address;

    @Column(name = "table_number")
    private Integer tableNumber; // Bàn số mấy đặt món
    
    // --- THÊM DÒNG NÀY ---
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String note; 
    // ---------------------
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items;
}