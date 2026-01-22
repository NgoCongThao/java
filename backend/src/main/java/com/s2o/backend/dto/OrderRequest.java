package com.s2o.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class OrderRequest {
    private Long tableId;       // Bàn nào gọi?
    private List<OrderItemRequest> items; // Danh sách món

    // Class con để hứng từng món
    @Data
    public static class OrderItemRequest {
        private Long productId; // ID món ăn
        private Integer quantity; // Số lượng
    }
}