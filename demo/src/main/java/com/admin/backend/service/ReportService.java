package com.admin.backend.service;

import com.admin.backend.repository.BillRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class ReportService {

    private final BillRepository billRepository;

    public ReportService(BillRepository billRepository) {
        this.billRepository = billRepository;
    }

    // Sửa phương thức này để khớp với BillRepository mới
    public BigDecimal getRevenue(Long tenantId, LocalDate from, LocalDate to) {
        // Gọi đúng tên hàm mới trong Repository: sumTotalAmountByDateRange
        // Và truyền tham số đúng thứ tự: (from, to, tenantId)
        return billRepository.sumTotalAmountByDateRange(from, to, tenantId);
    }
}