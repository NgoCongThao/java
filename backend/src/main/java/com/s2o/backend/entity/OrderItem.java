package com.s2o.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer quantity; // Số lượng
    private Double price;     // Giá tại thời điểm gọi (đề phòng sau này tăng giá menu)
    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING; // Mặc định món mới gọi là PENDING (Chờ)
    @ManyToOne
    @JoinColumn(name = "order_id")
    @JsonIgnore
    private Order order; // Thuộc về hóa đơn nào

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product; // Là món gì
}