package com.onlinestore.repository;

import com.onlinestore.model.Order;
import com.onlinestore.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // Найти заказы пользователя
    List<Order> findByUser(User user);
    
    // Найти заказы пользователя по ID
    List<Order> findByUserId(Long userId);
    
    // Найти заказы по статусу
    List<Order> findByStatus(String status);
    
    // Найти заказы пользователя по статусу
    List<Order> findByUserIdAndStatus(Long userId, String status);
    
    // Получить все заказы отсортированные по дате (новые сверху)
    List<Order> findAllByOrderByOrderDateDesc();
}
