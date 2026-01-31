// package com.s2o.backend_api.dto;

// import lombok.Data;
// import java.time.LocalDate;
// import java.time.LocalTime;

// @Data
// public class BookingRequest {
//     private Long restaurantId;
//     private Long userId;
//     private String customerName;
//     private String phone;
//     private LocalDate date; // Định dạng gửi lên: "2025-10-20"
//     private LocalTime time; // Định dạng gửi lên: "18:30"
//     private Integer tableNumber;// Bàn số mấy
//     private Integer guests;
//     private String note;
// }
package com.s2o.backend_api.dto;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class BookingRequest {
    private Long restaurantId;
    private Long userId;
    private String customerName;
    private String phone;
    private LocalDate date;
    private LocalTime time;
    private Integer tableNumber; // Bàn muốn đặt
    private Integer guests;
    private String note;
    
    // Danh sách món đặt trước
    private List<BookingItemRequest> items;

    @Data
    public static class BookingItemRequest {
        private String name;
        private Integer qty;
        private Double price;
    }
}