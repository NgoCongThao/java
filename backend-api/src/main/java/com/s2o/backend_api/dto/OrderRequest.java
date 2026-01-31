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
    // ... các field hiện có
    private Long restaurantId; // THÊM DÒNG NÀY
    private List<ItemRequest> items;

    @Data
    public static class ItemRequest {
        private String name;
        private Integer qty;
        private Double price;
    }
    
}