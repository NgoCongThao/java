package com.admin.backend.dto;

public class BookingCreateRequest {

    private String customer_name;
    private String phone;
    private String email;           
    private String date;
    private String time;
    private Integer num_guests;
    private String special_requests; 

    // --- GETTERS & SETTERS ---

    public String getCustomer_name() {
        return customer_name;
    }

    public void setCustomer_name(String customer_name) {
        this.customer_name = customer_name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

  
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Integer getNum_guests() {
        return num_guests;
    }

    public void setNum_guests(Integer num_guests) {
        this.num_guests = num_guests;
    }

   
    public String getSpecial_requests() {
        return special_requests;
    }

    public void setSpecial_requests(String special_requests) {
        this.special_requests = special_requests;
    }
}