package com.s2o.backend_api.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "restaurants")
@Data
public class Restaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String name;

    @Column(name = "image") // Khớp với DB mới
    private String image;

    private Double rating;

    @Column(columnDefinition = "NVARCHAR(100)")
    private String category;

    private String status;

    @Column(name = "is_open")
    private Boolean isOpen; // Java tự động map true/false

    private Double latitude;
    
    private Double longitude;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String address;

    @Column(name = "time") // Khớp với DB mới
    private String time;
    
    private String phone;
}