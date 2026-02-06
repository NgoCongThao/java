package com.s2o.backend_api.controller;

import com.s2o.backend_api.dto.TableStatusDTO;
import com.s2o.backend_api.service.TableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

// controller/TableController.java
@RestController
@RequestMapping("/api/staff/tables")
public class TableController {

    @Autowired
    private TableService tableService;

    @GetMapping("/status-at-time")
    public ResponseEntity<List<TableStatusDTO>> getStatusAtTime(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam("time") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time) {

        // Gọi service xử lý
        List<TableStatusDTO> result = tableService.getTableStatusAt(date, time);
        return ResponseEntity.ok(result);
    }
}