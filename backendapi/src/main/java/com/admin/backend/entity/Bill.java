package com.admin.backend.entity;

import jakarta.persistence.*;
import lombok.Data; 
import java.math.BigDecimal; 
import java.time.LocalDate;  

@Entity
@Table(name = "bills")
@Data 
public class Bill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

  
    private BigDecimal totalAmount;

    private String note;

   
    private LocalDate date; 

    private Long tenantId;
}