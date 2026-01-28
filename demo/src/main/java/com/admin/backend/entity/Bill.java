package com.admin.backend.entity;

import jakarta.persistence.*;
import lombok.Data; // Nếu bạn dùng Lombok
import java.math.BigDecimal; // ✅ QUAN TRỌNG: Import BigDecimal
import java.time.LocalDate;  // ✅ QUAN TRỌNG: Import LocalDate

@Entity
@Table(name = "bills")
@Data // Tự động sinh Getter/Setter. Nếu ko dùng Lombok thì phải tự viết getter/setter
public class Bill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ Phải là BigDecimal thì Controller mới hết lỗi
    private BigDecimal totalAmount;

    private String note;

    // ✅ Phải thêm trường này thì Controller mới gọi bill.setDate() được
    private LocalDate date; 

    private Long tenantId;
}