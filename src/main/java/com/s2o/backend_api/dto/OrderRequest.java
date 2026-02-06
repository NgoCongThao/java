package com.s2o.backend_api.dto;

import lombok.Data;
import java.util.List;

@Data
public class OrderRequest {
    private Long userId;
    private String restaurantName;
    private String note; // <--- Thêm dòng này
    private Integer tableNumber; // <--- Thêm dòng này
    private Double total;
    private String address;
    private String customerName;  // <--- Thêm dòng này để sửa lỗi
    private String phone;         // <--- Thêm dòng này (nếu chưa có)
    // ... các field hiện có
    private Long restaurantId; // THÊM DÒNG NÀY
    private List<ItemRequest> items;
    // --- CÁC TRƯỜNG MỚI CHO DELIVERY ---
    private String orderType;       // "DINE_IN", "DELIVERY"
    private String deliveryAddress; // Địa chỉ cụ thể
    private String Phone;   // SĐT người nhận
    private String desiredTime;     // Giờ hẹn giao (Dạng String ISO hoặc LocalDateTime)
    // --- THÊM ĐOẠN NÀY VÀO ---
    private Integer pointsToUse;

    public Integer getPointsToUse() {
        return pointsToUse;
    }

    public void setPointsToUse(Integer pointsToUse) {
        this.pointsToUse = pointsToUse;
    }
    // -----------------------------------
    @Data
    public static class ItemRequest {
        private String name;
        private Integer qty;
        private Double price;
    }
    
}