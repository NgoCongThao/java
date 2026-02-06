package com.s2o.backend_api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "order_items")
@Data // Lombok sẽ tự động sinh ra setStatus(), getStatus(), getItemName()...
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_name")
    private String itemName; // -> Lombok sinh ra hàm getItemName()

    private Integer quantity;
    private Double price;

    // --- QUAN TRỌNG: THÊM TRƯỜNG STATUS ---
    // Lưu trạng thái món ăn: PENDING (Chờ nấu), READY (Xong), SERVED (Đã phục vụ)
    // Mặc định khi tạo mới là "PENDING"
    private String status = "PENDING";

    @ManyToOne
    @JoinColumn(name = "order_id")
    @JsonIgnore
    private Order order;

    // --- GIẢI QUYẾT LỖI getMenuItemName ---
    // Vì trong Entity em đặt tên biến là "itemName", nhưng bên Controller em lỡ gọi "getMenuItemName()"
    // Cách 1: Sửa bên Controller thành item.getItemName() (Cách chuẩn nhất)
    // Cách 2: Thêm hàm phụ trợ (Alias) ở đây để code Controller không bị lỗi:
    public String getMenuItemName() {
        return this.itemName;
    }
}