package com.example.backend.controller;

import com.example.backend.repository.OrderRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RevenueController {

    private final OrderRepository orderRepository;

    public RevenueController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @GetMapping("/revenue")
    public Double getRevenue() {
        return orderRepository.sumRevenue();
    }
}
