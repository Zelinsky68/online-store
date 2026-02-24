package com.onlinestore.service;

import com.onlinestore.model.Order;
import com.onlinestore.model.OrderItem;
import com.onlinestore.model.Product;
import com.onlinestore.model.User;
import com.onlinestore.repository.OrderRepository;
import com.onlinestore.repository.ProductRepository;
import com.onlinestore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public Order createOrder(Long userId, String shippingAddress) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Исправлено: используем правильный конструктор
        Order order = new Order(user, shippingAddress);
        
        return orderRepository.save(order);
    }

    @Transactional
    public Order addItemToOrder(Long orderId, Long productId, Integer quantity) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        if (!order.canBeModified()) {
            throw new RuntimeException("Cannot modify order in status: " + order.getStatus());
        }
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        // Проверка наличия товара
        if (product.getQuantity() < quantity) {
            throw new RuntimeException("Not enough products in stock");
        }
        
        // Создаем позицию заказа
        OrderItem orderItem = new OrderItem(order, product, quantity);
        order.addItem(orderItem);
        
        // Уменьшаем количество товара на складе
        product.setQuantity(product.getQuantity() - quantity);
        productRepository.save(product);
        
        return orderRepository.save(order);
    }

    @Transactional
    public Order removeItemFromOrder(Long orderId, Long itemId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        if (!order.canBeModified()) {
            throw new RuntimeException("Cannot modify order in status: " + order.getStatus());
        }
        
        OrderItem itemToRemove = order.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item not found in order"));
        
        // Возвращаем товар на склад
        Product product = itemToRemove.getProduct();
        product.setQuantity(product.getQuantity() + itemToRemove.getQuantity());
        productRepository.save(product);
        
        order.removeItem(itemToRemove);
        
        return orderRepository.save(order);
    }

    @Transactional
    public Order updateItemQuantity(Long orderId, Long itemId, Integer newQuantity) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        if (!order.canBeModified()) {
            throw new RuntimeException("Cannot modify order in status: " + order.getStatus());
        }
        
        OrderItem item = order.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item not found in order"));
        
        Product product = item.getProduct();
        int quantityDiff = newQuantity - item.getQuantity();
        
        // Проверяем наличие товара при увеличении количества
        if (quantityDiff > 0 && product.getQuantity() < quantityDiff) {
            throw new RuntimeException("Not enough products in stock");
        }
        
        // Обновляем складские остатки
        product.setQuantity(product.getQuantity() - quantityDiff);
        productRepository.save(product);
        
        // Обновляем количество в заказе
        item.setQuantity(newQuantity);
        
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    @Transactional(readOnly = true)
    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        switch (status.toUpperCase()) {
            case "PROCESSING":
                order.process();
                break;
            case "SHIPPED":
                order.ship();
                break;
            case "DELIVERED":
                order.deliver();
                break;
            case "CANCELLED":
                if (order.canBeCancelled()) {
                    // Возвращаем товары на склад при отмене
                    for (OrderItem item : order.getItems()) {
                        Product product = item.getProduct();
                        product.setQuantity(product.getQuantity() + item.getQuantity());
                        productRepository.save(product);
                    }
                }
                order.cancel();
                break;
            default:
                throw new RuntimeException("Invalid status: " + status);
        }
        
        return orderRepository.save(order);
    }

    @Transactional
    public void deleteOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        if (!order.canBeModified()) {
            throw new RuntimeException("Cannot delete order in status: " + order.getStatus());
        }
        
        // Возвращаем товары на склад
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setQuantity(product.getQuantity() + item.getQuantity());
            productRepository.save(product);
        }
        
        orderRepository.delete(order);
    }
}
