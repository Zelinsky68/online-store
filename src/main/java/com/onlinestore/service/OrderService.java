package com.onlinestore.service;

import com.onlinestore.model.Order;
import com.onlinestore.model.User;
import com.onlinestore.repository.OrderRepository;
import com.onlinestore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // Получить все заказы
    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc();
    }
    
    // Получить заказ по ID
    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }
    
    // Получить заказы пользователя
    public List<Order> getOrdersByUser(Long userId) {
        return orderRepository.findByUserId(userId);
    }
    
    // Создать новый заказ
    public Order createOrder(Long userId, Double totalAmount, String shippingAddress) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new IllegalArgumentException("User not found with id: " + userId);
        }
        
        Order order = new Order(user, totalAmount, shippingAddress);
        return orderRepository.save(order);
    }
    
    // Обновить статус заказа
    public Order updateOrderStatus(Long orderId, String newStatus) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            throw new IllegalArgumentException("Order not found with id: " + orderId);
        }
        
        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }
    
    // Отменить заказ
    public Order cancelOrder(Long orderId) {
        return updateOrderStatus(orderId, "CANCELLED");
    }
    
    // Удалить заказ
    public void deleteOrder(Long orderId) {
        orderRepository.deleteById(orderId);
    }
    
    // Получить заказы по статусу
    public List<Order> getOrdersByStatus(String status) {
        return orderRepository.findByStatus(status);
    }
    
    // Получить заказы пользователя по статусу
    public List<Order> getUserOrdersByStatus(Long userId, String status) {
        return orderRepository.findByUserIdAndStatus(userId, status);
    }
}
