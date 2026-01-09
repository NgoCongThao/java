package com.s2o.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "dining_tables")
public class DiningTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // Bàn 1, Bàn 2...
    private String status = "AVAILABLE"; // AVAILABLE, OCCUPIED, RESERVED
    private String qrCode;

    @ManyToOne
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;
}