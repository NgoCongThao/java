package com.admin.backend.service;

import com.admin.backend.entity.Bill;
import com.admin.backend.repository.BillRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal; // ✅ Import này
import java.time.LocalDate;
import java.util.List;

@Service
public class BillService {

    private final BillRepository billRepository;

    public BillService(BillRepository billRepository) {
        this.billRepository = billRepository;
    }

    public Bill createBill(Bill bill, Long tenantId) {
        bill.setTenantId(tenantId);
        // Nếu chưa có ngày thì set là hôm nay (đề phòng)
        if (bill.getDate() == null) {
            bill.setDate(LocalDate.now());
        }
        return billRepository.save(bill);
    }

    public List<Bill> getBillsByDate(LocalDate date, Long tenantId) {
        return billRepository.findByDateAndTenantId(date, tenantId);
    }

    // --- SỬA HÀM NÀY: Trả về BigDecimal ---
    public BigDecimal getRevenue(LocalDate from, LocalDate to, Long tenantId) {
        return billRepository.sumTotalAmountByDateRange(from, to, tenantId);
    }
}