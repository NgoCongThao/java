package com.s2o.backend_api.dto;

import lombok.Data;

@Data
public class RestaurantUpdateRequest {
    private String name;
    private String address;
    private String phone;
    private String description;
    private String image;       // Link ảnh
    private String time;        // Giờ mở cửa
    private String category;    // Thể loại
    private Boolean isOpen;     // Trạng thái mở/đóng
    private Integer totalTables;
    private Double latitude;
    private Double longitude;
}