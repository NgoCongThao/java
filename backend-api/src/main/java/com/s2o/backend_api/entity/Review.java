package com.s2o.backend_api.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Data
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "menu_item_id")
    private Long menuItemId;

    private Integer rating;
    private String comment;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    // Lưu tạm tên người dùng để hiển thị cho nhanh (khỏi join bảng User)
    @Transient 
    private String userName; 
}