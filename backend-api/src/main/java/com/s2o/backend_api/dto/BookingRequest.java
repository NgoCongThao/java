package com.s2o.backend_api.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class BookingRequest {
    private Long restaurantId;
    private Long userId;
    private String customerName;
    private String phone;
    private LocalDate date; // Định dạng gửi lên: "2025-10-20"
    private LocalTime time; // Định dạng gửi lên: "18:30"
    private Integer guests;
    private String note;
}