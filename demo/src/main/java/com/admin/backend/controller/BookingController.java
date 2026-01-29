package com.admin.backend.controller;

import com.admin.backend.dto.BookingCreateRequest;
import com.admin.backend.entity.Booking;
import com.admin.backend.service.BookingService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

 
    @GetMapping
    public ResponseEntity<?> getBookings(HttpServletRequest request) {
        Long tenantId = (Long) request.getAttribute("tenantId");
        if (tenantId == null) {
            return ResponseEntity.status(401).body("Lỗi: Chưa đăng nhập hoặc Token không hợp lệ");
        }
        return ResponseEntity.ok(bookingService.getAll(tenantId));
    }


    @PostMapping
    public ResponseEntity<?> createBooking(
            @RequestBody BookingCreateRequest bookingRequest, // Hứng cục JSON bằng DTO
            HttpServletRequest request
    ) {
        try {
            Long tenantId = (Long) request.getAttribute("tenantId");
            if (tenantId == null) {
                return ResponseEntity.status(401).body("Lỗi: Chưa đăng nhập");
            }

            
            Booking newBooking = bookingService.create(bookingRequest, tenantId);
            
            return ResponseEntity.ok(newBooking);

        } catch (Exception e) {
            e.printStackTrace(); // In lỗi ra Terminal để debug
            return ResponseEntity.badRequest().body("Lỗi tạo booking: " + e.getMessage());
        }
    }


    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateBookingStatus(
            @PathVariable("id") Integer bookingId,
            @RequestParam("status") String status, // Nhận status từ URL (?status=CONFIRMED)
            HttpServletRequest request
    ) {
        try {
            Long tenantId = (Long) request.getAttribute("tenantId");
            if (tenantId == null) {
                return ResponseEntity.status(401).body("Lỗi: Chưa đăng nhập");
            }

            Booking updatedBooking = bookingService.updateStatus(bookingId, status, tenantId);
            return ResponseEntity.ok(updatedBooking);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBookingInfo(
            @PathVariable Integer id,
            @RequestBody BookingCreateRequest req,
            HttpServletRequest request
    ) {
        try {
            Long tenantId = (Long) request.getAttribute("tenantId");
            if (tenantId == null) return ResponseEntity.status(401).body("Chưa đăng nhập");

            Booking updated = bookingService.updateInfo(id, req, tenantId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

 
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBooking(@PathVariable Integer id, HttpServletRequest request) {
        try {
            Long tenantId = (Long) request.getAttribute("tenantId");
            if (tenantId == null) return ResponseEntity.status(401).body("Chưa đăng nhập");

            bookingService.delete(id, tenantId);
            return ResponseEntity.ok("Đã xóa thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
}
