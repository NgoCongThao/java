package com.admin.backend.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @GetMapping("/profile")
    public String profile(Authentication authentication) {
        return "Hello admin: " + authentication.getName();
    }
}