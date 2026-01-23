package com.example.backend.service;

import com.example.backend.entity.Booking;
import com.example.backend.entity.Tenant;
import com.example.backend.repository.BookingRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    public List<Booking> getAll(Long tenantId) {
        return bookingRepository.findByTenantId(tenantId);
    }

    public Booking create(Booking booking, Tenant tenant) {
        booking.setTenant(tenant);
        return bookingRepository.save(booking);
    }
}
