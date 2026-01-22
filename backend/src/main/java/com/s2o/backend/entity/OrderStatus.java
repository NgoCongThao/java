package com.s2o.backend.entity;

public enum OrderStatus {
    PENDING,    // Chờ bếp xác nhận
    COOKING,    // Bếp đang nấu
    READY,      // Đã nấu xong (Chờ phục vụ bưng ra)
    DELIVERED,  // Đã lên món (Khách đang ăn)
    PAID,       // Đã thanh toán
    CANCELLED   // Đã hủy
}