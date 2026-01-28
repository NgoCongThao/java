package com.admin.backend.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String username;

    private String password;
    
    @JsonProperty("full_name") // Jackson dùng cái này để đọc từ Postman
    @Column(name = "full_name") // Hibernate dùng cái này để lưu vào DB
    private String fullName;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "tenant_id")
    private Long tenantId;

    public enum Role {
        manager,
        chef,
        waiter
    }
}