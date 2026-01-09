package com.s2o.backend; // <--- QUAN TRỌNG: Phải đúng package này

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class S2oApplication {
    public static void main(String[] args) {
        SpringApplication.run(S2oApplication.class, args);
    }
}