package com.s2o.backend_api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Data
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Liên kết với Nhà hàng (Khách đặt bàn ở quán nào)
    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    @JsonIgnore
    private Restaurant restaurant;

    // Liên kết với User (Ai đặt)
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "customer_name")
    private String customerName;

    private String phone;

    @Column(name = "booking_date")
    private LocalDate bookingDate; // Ngày đặt (YYYY-MM-DD)

    @Column(name = "booking_time")
    private LocalTime bookingTime; // Giờ đặt (HH:MM:SS)

    @Column(name = "guest_count")
    private Integer guestCount;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String note;

    private String status; // Vd: "PENDING", "CONFIRMED", "CANCELLED"

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
}