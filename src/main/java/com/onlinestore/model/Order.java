package com.onlinestore.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    @Column(length = 20)
    private String status = "PENDING";

    @Column(name = "shipping_address", columnDefinition = "TEXT")
    private String shippingAddress;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Связь с OrderItem
    @JsonIgnore
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, 
               orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();

    // Конструкторы
    public Order() {
        this.orderDate = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.totalAmount = 0.0;
    }

    public Order(User user) {
        this();
        this.user = user;
    }

    public Order(User user, String shippingAddress) {
        this(user);
        this.shippingAddress = shippingAddress;
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
        recalculateTotalAmount();
    }

    // Бизнес-методы
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
        recalculateTotalAmount();
    }

    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
        recalculateTotalAmount();
    }

    public void clearItems() {
        items.clear();
        recalculateTotalAmount();
    }

    private void recalculateTotalAmount() {
        this.totalAmount = items.stream()
                .mapToDouble(item -> item.getPrice().doubleValue() * item.getQuantity())
                .sum();
    }

    public int getItemCount() {
        return items.stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();
    }

    public boolean isPending() {
        return "PENDING".equals(status);
    }

    public boolean isProcessing() {
        return "PROCESSING".equals(status);
    }

    public boolean isShipped() {
        return "SHIPPED".equals(status);
    }

    public boolean isDelivered() {
        return "DELIVERED".equals(status);
    }

    public boolean isCancelled() {
        return "CANCELLED".equals(status);
    }

    public boolean canBeCancelled() {
        return isPending() || isProcessing();
    }

    public boolean canBeModified() {
        return isPending();
    }

    // Методы для обновления статуса
    public void process() {
        if (!isPending()) {
            throw new IllegalStateException("Заказ можно обработать только в статусе PENDING");
        }
        this.status = "PROCESSING";
    }

    public void ship() {
        if (!isProcessing()) {
            throw new IllegalStateException("Отправить можно только заказ в статусе PROCESSING");
        }
        this.status = "SHIPPED";
    }

    public void deliver() {
        if (!isShipped()) {
            throw new IllegalStateException("Доставить можно только отправленный заказ");
        }
        this.status = "DELIVERED";
    }

    public void cancel() {
        if (!canBeCancelled()) {
            throw new IllegalStateException("Нельзя отменить заказ в статусе " + status);
        }
        this.status = "CANCELLED";
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (orderDate == null) {
            orderDate = LocalDateTime.now();
        }
        if (status == null) {
            status = "PENDING";
        }
        if (totalAmount == null) {
            totalAmount = 0.0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", user=" + (user != null ? user.getUsername() : "null") +
                ", orderDate=" + orderDate +
                ", totalAmount=" + totalAmount +
                ", status='" + status + '\'' +
                ", itemsCount=" + items.size() +
                '}';
    }
}
