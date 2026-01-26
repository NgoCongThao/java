package com.admin.backend.service;


import com.admin.backend.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ReportService {

    private final OrderRepository orderRepository;

    public ReportService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public BigDecimal getRevenue(Long tenantId, String from, String to) {
        return orderRepository.sumRevenue(tenantId, from, to);
    }
}