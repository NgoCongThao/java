package com.s2o.backend_api.dto;

// dto/TableStatusDTO.java
public class TableStatusDTO {
    private int number;
    private String status; // "FREE" hoặc "BUSY"
    private String reason; // Lý do bận (VD: "Booking 18:00", "Đang ăn")

    // Constructor, Getters, Setters
    public TableStatusDTO(int number, String status, String reason) {
        this.number = number;
        this.status = status;
        this.reason = reason;
    }
    // 3. Getters and Setters
    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}