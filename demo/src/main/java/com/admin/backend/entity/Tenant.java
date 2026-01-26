package com.admin.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tenants")
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String status;

    // getter / setter
    public Long getId() {
return id;
}


public String getName() {
return name;
}


public void setName(String name) {
this.name = name;
}


public String getStatus() {
return status;
}


public void setStatus(String status) {
this.status = status;
}
}
