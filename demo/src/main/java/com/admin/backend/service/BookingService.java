package com.admin.backend.service;
import com.admin.backend.dto.BookingCreateRequest;
import com.admin.backend.entity.Booking;
import com.admin.backend.repository.BookingRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;

    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    /**
     * Lấy toàn bộ booking theo tenant
     */
    public List<Booking> getAll(Long tenantId) {
        return bookingRepository.findByTenantId(tenantId);
    }

public Booking create(BookingCreateRequest req, Long tenantId) {
    Booking booking = new Booking();

    // Map dữ liệu từ DTO sang Entity
    booking.setCustomerName(req.getCustomer_name());
    booking.setPhone(req.getPhone());
    booking.setEmail(req.getEmail()); // ✅ Mới thêm
    
    booking.setBookingDate(LocalDate.parse(req.getDate()));
    booking.setBookingTime(LocalTime.parse(req.getTime()));
    booking.setNumGuests(req.getNum_guests());
    
    booking.setSpecialRequests(req.getSpecial_requests()); // ✅ Mới thêm
    
    booking.setStatus("PENDING");
    booking.setTenantId(tenantId);

    return bookingRepository.save(booking);
}
    /**
     * Cập nhật trạng thái booking (có kiểm tra tenant)
     */
    public Booking updateStatus(Integer bookingId, String status, Long tenantId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new RuntimeException("Booking không tồn tại")
                );

        // Đảm bảo booking thuộc đúng tenant
        if (!booking.getTenantId().equals(tenantId)) {
            throw new RuntimeException("Không có quyền truy cập booking này");
        }

        booking.setStatus(status);
        return bookingRepository.save(booking);
    }
    // 4. Cập nhật thông tin đơn đặt (Sửa tên, ngày, giờ...)
    public Booking updateInfo(Integer id, BookingCreateRequest req, Long tenantId) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking không tồn tại"));

        if (!booking.getTenantId().equals(tenantId)) {
            throw new RuntimeException("Không có quyền sửa đơn này");
        }

        // Map dữ liệu mới vào
        booking.setCustomerName(req.getCustomer_name());
        booking.setPhone(req.getPhone());
        booking.setEmail(req.getEmail());
        booking.setBookingDate(LocalDate.parse(req.getDate()));
        booking.setBookingTime(LocalTime.parse(req.getTime()));
        booking.setNumGuests(req.getNum_guests());
        booking.setSpecialRequests(req.getSpecial_requests());

        return bookingRepository.save(booking);
    }

    // 5. Xóa đơn đặt
    public void delete(Integer id, Long tenantId) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking không tồn tại"));

        if (!booking.getTenantId().equals(tenantId)) {
            throw new RuntimeException("Không có quyền xóa đơn này");
        }

        bookingRepository.delete(booking);
    }
}
