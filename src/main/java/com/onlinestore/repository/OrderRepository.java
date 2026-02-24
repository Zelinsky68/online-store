package com.onlinestore.repository;

import com.onlinestore.model.Order;
import com.onlinestore.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
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

    // ДОПОЛНИТЕЛЬНЫЕ ПОЛЕЗНЫЕ МЕТОДЫ:

    // Найти заказы за период
    List<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Найти заказы пользователя за период
    List<Order> findByUserIdAndOrderDateBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    // Подсчитать количество заказов пользователя
    long countByUserId(Long userId);

    // Подсчитать количество заказов по статусу
    long countByStatus(String status);

    // Найти заказы с суммой больше указанной
    List<Order> findByTotalAmountGreaterThan(Double amount);

    // Найти последние N заказов пользователя
    List<Order> findTop10ByUserIdOrderByOrderDateDesc(Long userId);

    // Найти заказы, содержащие определенный товар (через JPQL запрос)
    @Query("SELECT DISTINCT o FROM Order o JOIN o.items i WHERE i.product.id = :productId")
    List<Order> findByProductId(@Param("productId") Long productId);

    // Найти заказы, содержащие определенный товар для конкретного пользователя
    @Query("SELECT DISTINCT o FROM Order o JOIN o.items i WHERE o.user.id = :userId AND i.product.id = :productId")
    List<Order> findByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);

    // Получить статистику по заказам пользователя
    @Query("SELECT COUNT(o), COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.user.id = :userId")
    Object[] getOrderStatsByUserId(@Param("userId") Long userId);

    // Найти заказы, которые можно отменить (PENDING или PROCESSING)
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.status IN ('PENDING', 'PROCESSING')")
    List<Order> findCancellableOrdersByUser(@Param("userId") Long userId);

    // Поиск заказов по части адреса доставки
    List<Order> findByShippingAddressContainingIgnoreCase(String addressPart);

    // Найти все заказы с определенным статусом, отсортированные по дате
    List<Order> findByStatusOrderByOrderDateDesc(String status);

    // Проверить, есть ли у пользователя заказы
    boolean existsByUserId(Long userId);
}
