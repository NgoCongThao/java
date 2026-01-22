package com.s2o.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "orders") // "order" là từ khóa của SQL nên phải đặt tên khác, vd "orders"
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Thời gian tạo đơn
    private LocalDateTime createdAt = LocalDateTime.now();

    // Tổng tiền tạm tính
    private Double totalAmount;

    // Trạng thái đơn hàng
    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING;

    // Bàn nào gọi?
    @ManyToOne
    @JoinColumn(name = "table_id")
    private DiningTable table;

    // Nhà hàng nào? (SaaS)
    @ManyToOne
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    // Danh sách các món trong đơn này
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items = new ArrayList<>();
}