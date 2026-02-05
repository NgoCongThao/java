package com.s2o.backend_api.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String fullName;
    private String phone;
    private String role;
    // THÊM TRƯỜNG NÀY (có thể null cho khách thường)
    private Long restaurantId;


}