package com.admin.backend.controller;

import com.admin.backend.entity.Booking;
import com.admin.backend.service.BookingService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * Lấy danh sách booking theo tenant
     */
    @GetMapping
    public List<Booking> getBookings(HttpServletRequest request) {
        Long tenantId = (Long) request.getAttribute("tenantId");
        return bookingService.getAll(tenantId);
    }

    /**
     * Cập nhật trạng thái booking
     */
    @PutMapping("/{id}/status")
    public Booking updateBookingStatus(
            @PathVariable("id") Integer bookingId,
            @RequestParam("status") String status,
            HttpServletRequest request
    ) {
        Long tenantId = (Long) request.getAttribute("tenantId");
        return bookingService.updateStatus(bookingId, status, tenantId);
    }
}