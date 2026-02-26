package com.onlinestore.dto;

import java.time.LocalDateTime;

public class OrderDto {
    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private LocalDateTime orderDate;
    private Double totalAmount;
    private String status;
    private String shippingAddress;

    // Конструкторы
    public OrderDto() {}

    public OrderDto(Long id, Long userId, String userName, String userEmail, 
                   LocalDateTime orderDate, Double totalAmount, String status, 
                   String shippingAddress) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.status = status;
        this.shippingAddress = shippingAddress;
    }

    // Геттеры
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getUserEmail() { return userEmail; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public Double getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
    public String getShippingAddress() { return shippingAddress; }

    // Сеттеры
    public void setId(Long id) { this.id = id; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    public void setStatus(String status) { this.status = status; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
}
