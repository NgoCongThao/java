package com.admin.backend.service;

import  com.admin.backend.entity.Booking;
import  com.admin.backend.repository.BookingRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;

    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public List<Booking> getAll(Long tenantId) {
        return bookingRepository.findByTenantId(tenantId);
    }

    public Booking updateStatus(Integer id, String status, Long tenantId) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking không tồn tại"));

        // đảm bảo đúng tenant
        if (!booking.getTenantId().equals(tenantId)) {
            throw new RuntimeException("Không có quyền truy cập booking này");
        }

        booking.setStatus(status);
        return bookingRepository.save(booking);
    }
}