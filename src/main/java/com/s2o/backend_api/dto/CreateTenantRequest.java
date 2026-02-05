package com.s2o.backend_api.dto;
import lombok.Data;

@Data
public class CreateTenantRequest {
    // Thông tin nhà hàng
    private String restaurantName;
    private String address;
    private String phone;

    // Thông tin tài khoản chủ quán (Manager)
    private String managerUsername;
    private String managerPassword;
    private String managerFullName;
}