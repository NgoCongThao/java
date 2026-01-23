package com.example.backend.entity;

import jakarta.persistence.*;

@Entity(name = "OrderEntity")
@Table(name = "ORDERS")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer quantity;

    @ManyToOne
    @JoinColumn(name = "menu_id")
    private Menu menu;

    public Order() {
    }

    public Order(Integer quantity, Menu menu) {
        this.quantity = quantity;
        this.menu = menu;
    }

    public Long getId() {
        return id;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Menu getMenu() {
        return menu;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
    }
}
