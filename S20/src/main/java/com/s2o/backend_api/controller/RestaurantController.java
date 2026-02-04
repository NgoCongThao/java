package com.s2o.backend_api.controller;

import com.s2o.backend_api.entity.Restaurant;
import com.s2o.backend_api.repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/guest")
@CrossOrigin(origins = "*")
public class RestaurantController {

    @Autowired
    private RestaurantRepository restaurantRepository;

    // API: Lấy danh sách tất cả nhà hàng
    // URL: http://localhost:8080/api/guest/restaurants
    @GetMapping("/restaurants")
    public List<Restaurant> getAllRestaurants() {
        return restaurantRepository.findAll();
    }
}