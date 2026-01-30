
package com.s2o.backend_api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Data
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String comment;

    private Integer rating;

    @Column(name = "created_at")
    @CreationTimestamp // Tự động lấy giờ hiện tại khi lưu
    private LocalDateTime createdAt;

    // QUAN TRỌNG: Liên kết với bảng User để lấy tên người dùng
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Liên kết với Món ăn
    @ManyToOne
    @JoinColumn(name = "menu_item_id", nullable = false)
    @JsonIgnore // Ngắt vòng lặp JSON
    private MenuItem menuItem;
}