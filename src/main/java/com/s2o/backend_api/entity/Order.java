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

    // SỬA: Thêm NVARCHAR để lưu tên quán tiếng Việt
    @Column(name = "restaurant_name", columnDefinition = "NVARCHAR(255)")
    private String restaurantName;

    @Column(name = "restaurant_id")
    private Long restaurantId;

    @Column(name = "total_price")
    private Double totalPrice;
    @Column(name = "final_price")
    private Double finalPrice;
    public Double getFinalPrice() { return finalPrice; }
    public void setFinalPrice(Double finalPrice) { this.finalPrice = finalPrice; }
    // SỬA: Thêm NVARCHAR cho trạng thái
    @Column(columnDefinition = "NVARCHAR(50)")
    private String status;
    @Column(name = "points_used")
    private Integer pointsUsed; // Số điểm đã dùng

    // Getter & Setter
    public Integer getPointsUsed() { return pointsUsed; }
    public void setPointsUsed(Integer pointsUsed) { this.pointsUsed = pointsUsed; }
    // SỬA: Thêm NVARCHAR cho địa chỉ
    @Column(columnDefinition = "NVARCHAR(255)")
    private String address;

    @Column(name = "table_number")
    private Integer tableNumber;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String note;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items;

    // SỬA: Dùng NVARCHAR cho loại đơn để an toàn
    @Column(name = "order_type", columnDefinition = "NVARCHAR(50)")
    private String orderType; // "DINE_IN", "DELIVERY", "TAKEAWAY"

    // SỬA: Quan trọng - Địa chỉ giao hàng tiếng Việt
    @Column(name = "delivery_address", columnDefinition = "NVARCHAR(255)")
    private String deliveryAddress;

    @Column(name = "desired_time")
    private LocalDateTime desiredTime;

    @Column(name = "customer_phone")
    private String customerPhone;

    // SỬA: Quan trọng - Tên khách hàng tiếng Việt
    @Column(name = "customer_name", columnDefinition = "NVARCHAR(255)")
    private String customerName;
}