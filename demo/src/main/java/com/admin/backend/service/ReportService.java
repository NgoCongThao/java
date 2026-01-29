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

  
    public BigDecimal getRevenue(Long tenantId, LocalDate from, LocalDate to) {
    
        return billRepository.sumTotalAmountByDateRange(from, to, tenantId);
    }
}