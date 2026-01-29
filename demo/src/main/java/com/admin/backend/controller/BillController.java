package com.admin.backend.controller;

import com.admin.backend.entity.Bill;
import com.admin.backend.service.BillService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal; // ✅ Import quan trọng
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/bills")
public class BillController {

    private final BillService billService;

    public BillController(BillService billService) {
        this.billService = billService;
    }

    // 1. Tạo hóa đơn (POST)
    @PostMapping
    public ResponseEntity<?> createBill(@RequestBody Bill bill, HttpServletRequest req) {
        try {
            Long tenantId = (Long) req.getAttribute("tenantId");
            if (tenantId == null) return ResponseEntity.status(401).body("Chưa đăng nhập");
            
            return ResponseEntity.ok(billService.createBill(bill, tenantId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi tạo hóa đơn: " + e.getMessage());
        }
    }

    // 2. Lấy danh sách hóa đơn theo ngày (GET)
    @GetMapping
public ResponseEntity<?> getBills(
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate date,
        HttpServletRequest req) {

    Long tenantId = (Long) req.getAttribute("tenantId");
    if (tenantId == null) {
        return ResponseEntity.status(401).body("Chưa đăng nhập");
    }

    // 👉 Có date thì lọc, không có thì lấy tất cả
    if (date != null) {
        return ResponseEntity.ok(
            billService.getBillsByDate(date, tenantId)
        );
    }

    return ResponseEntity.ok(
        billService.getAllBills(tenantId)
    );
}

    // 3. Báo cáo doanh thu (GET /revenue) -> Trả về BigDecimal
    @GetMapping("/revenue")
    public ResponseEntity<?> getRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            HttpServletRequest req) {
        
        Long tenantId = (Long) req.getAttribute("tenantId");
        if (tenantId == null) return ResponseEntity.status(401).body("Chưa đăng nhập");

        // Service trả về BigDecimal -> Controller trả về Client
        BigDecimal total = billService.getRevenue(from, to, tenantId);
        
        // Trả về JSON: { "revenue": 500000 }
        return ResponseEntity.ok(Map.of("revenue", total));
    }

    @DeleteMapping("/{id}")
public ResponseEntity<?> deleteBill(
        @PathVariable Long id,
        HttpServletRequest req
) {
    Long tenantId = (Long) req.getAttribute("tenantId");
    if (tenantId == null) {
        return ResponseEntity.status(401).body("Chưa đăng nhập");
    }

    billService.deleteById(id, tenantId);
    return ResponseEntity.ok("Đã xóa hóa đơn");
}

}