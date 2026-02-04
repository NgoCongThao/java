package com.s2o.backend_api.dto;

import lombok.Data;
import java.util.List;

@Data
public class OrderRequest {
    private Long userId;
    private String restaurantName;
    private Double total;
    private String address;
    private List<ItemRequest> items;

    @Data
    public static class ItemRequest {
        private String name;
        private Integer qty;
        private Double price;
    }
}