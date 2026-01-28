package com.admin.backend.repository;

import com.admin.backend.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal; // ✅ Import này
import java.time.LocalDate;
import java.util.List;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {

    // Tìm hóa đơn theo ngày và tenant
    List<Bill> findByDateAndTenantId(LocalDate date, Long tenantId);

    // Tính tổng doanh thu (Sửa Double -> BigDecimal)
    // Lưu ý: COALESCE để nếu không có đơn nào thì trả về 0 thay vì null
    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Bill b WHERE b.date BETWEEN :from AND :to AND b.tenantId = :tenantId")
    BigDecimal sumTotalAmountByDateRange(
        @Param("from") LocalDate from, 
        @Param("to") LocalDate to, 
        @Param("tenantId") Long tenantId
    );
}