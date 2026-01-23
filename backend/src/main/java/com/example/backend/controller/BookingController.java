package com.example.backend.controller;

import com.example.backend.entity.Booking;
import com.example.backend.entity.Tenant;
import com.example.backend.repository.TenantRepository;
import com.example.backend.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private TenantRepository tenantRepository;

    @GetMapping
    public List<Booking> getAll(
            @RequestHeader("X-Tenant-Id") Long tenantId
    ) {
        return bookingService.getAll(tenantId);
    }

    @PostMapping
    public Booking create(
            @RequestHeader("X-Tenant-Id") Long tenantId,
            @RequestBody Booking booking
    ) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        return bookingService.create(booking, tenant);
    }
}
