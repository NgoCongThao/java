package com.s2o.backend_api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // Import mới
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "bookings")
@Data
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- QUAN TRỌNG: ĐÃ SỬA ĐOẠN NÀY ĐỂ LẤY DỮ LIỆU NHÀ HÀNG ---
    @ManyToOne(fetch = FetchType.EAGER) // Lấy luôn dữ liệu nhà hàng
    @JoinColumn(name = "restaurant_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // Cho phép Jackson serialize object này
    private Restaurant restaurant;
    // -----------------------------------------------------------

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "customer_name")
    private String customerName;

    private String phone;

    @Column(name = "booking_date")
    private LocalDate bookingDate;

    @Column(name = "booking_time")
    private LocalTime bookingTime;

    @Column(name = "guest_count")
    private Integer guestCount;

    @Column(name = "table_number")
    private Integer tableNumber;

     @Column(name = "total_price") //Huy thêm dòng này để tính tổng tiền
    private Double totalPrice = 0.0;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String note;

    private String status;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    private List<BookingItem> items;

    // --- HÀM TIỆN ÍCH CHO FRONTEND (DỰ PHÒNG) ---
    public String getRestaurantName() {
        return restaurant != null ? restaurant.getName() : "";
    }

    public String getRestaurantImage() {
        return restaurant != null ? restaurant.getImage() : "";
    }
}