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


// nếu client không gửi date thì set ngày hôm nay
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
    public List<Bill> getAllBills(Long tenantId) {
    return billRepository.findByTenantId(tenantId);
}

public void deleteById(Long id, Long tenantId) {
    Bill bill = billRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Bill không tồn tại"));

    if (!bill.getTenantId().equals(tenantId)) {
        throw new RuntimeException("Không có quyền xóa bill này");
    }

    billRepository.delete(bill);
}
}