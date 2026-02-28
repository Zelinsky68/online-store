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

    // ============= БАЗОВЫЕ МЕТОДЫ ПОИСКА =============
    
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

    // Найти заказы, содержащие определенный товар
    @Query("SELECT DISTINCT o FROM Order o JOIN o.items i WHERE i.product.id = :productId")
    List<Order> findByProductId(@Param("productId") Long productId);

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

    // ============= СТАТИСТИКА =============
    
    // Количество заказов за период
    long countByOrderDateBetween(LocalDateTime start, LocalDateTime end);
    
    // Количество заказов после даты
    long countByOrderDateAfter(LocalDateTime date);
    
    // Статистика по статусам
    @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    List<Object[]> countOrdersByStatus();
    
    // Средняя сумма заказа
    @Query("SELECT AVG(o.totalAmount) FROM Order o")
    Double getAverageOrderAmount();
    
    // Общая выручка (сумма всех заказов)
    @Query("SELECT SUM(o.totalAmount) FROM Order o")
    Double getTotalRevenue();
    
    // Выручка за период
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.orderDate BETWEEN :start AND :end")
    Double getRevenueBetweenDates(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    // Максимальная сумма заказа
    @Query("SELECT MAX(o.totalAmount) FROM Order o")
    Double getMaxOrderAmount();
    
    // Минимальная сумма заказа
    @Query("SELECT MIN(o.totalAmount) FROM Order o")
    Double getMinOrderAmount();
    
    // Количество заказов по дням за последние 30 дней (ИСПРАВЛЕНО)
    @Query("SELECT CAST(o.orderDate AS date), COUNT(o), SUM(o.totalAmount) FROM Order o " +
           "WHERE o.orderDate >= :startDate GROUP BY CAST(o.orderDate AS date) ORDER BY CAST(o.orderDate AS date)")
    List<Object[]> getDailyStats(@Param("startDate") LocalDateTime startDate);
    
    // Альтернативный вариант с native query (если предыдущий не работает)
    /*
    @Query(value = "SELECT DATE(o.order_date) as day, COUNT(*) as count, SUM(o.total_amount) as total " +
           "FROM orders o WHERE o.order_date >= :startDate " +
           "GROUP BY DATE(o.order_date) ORDER BY day", nativeQuery = true)
    List<Object[]> getDailyStatsNative(@Param("startDate") LocalDateTime startDate);
    */
    
    // Количество отмененных заказов
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'CANCELLED'")
    long countCancelledOrders();
    
    // Количество доставленных заказов
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'DELIVERED'")
    long countDeliveredOrders();
    
    // Количество заказов в обработке
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'PROCESSING'")
    long countProcessingOrders();
    
    // Количество ожидающих заказов
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'PENDING'")
    long countPendingOrders();
    
    // Общая выручка за текущий месяц
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE " +
           "o.orderDate >= :startOfMonth AND o.orderDate <= :endOfMonth")
    Double getMonthlyRevenue(@Param("startOfMonth") LocalDateTime startOfMonth, 
                             @Param("endOfMonth") LocalDateTime endOfMonth);
    
    // Топ пользователей по сумме заказов
    @Query("SELECT o.user.id, o.user.username, SUM(o.totalAmount) as total " +
           "FROM Order o GROUP BY o.user.id, o.user.username ORDER BY total DESC")
    List<Object[]> getTopUsersBySpending();
    
    // Топ товаров по продажам
    @Query("SELECT i.product.id, i.product.name, SUM(i.quantity), SUM(i.price * i.quantity) " +
           "FROM OrderItem i GROUP BY i.product.id, i.product.name ORDER BY SUM(i.quantity) DESC")
    List<Object[]> getTopProductsBySales();
    
    // Средний чек по дням
    @Query("SELECT CAST(o.orderDate AS date), AVG(o.totalAmount) FROM Order o " +
           "WHERE o.orderDate >= :startDate GROUP BY CAST(o.orderDate AS date) ORDER BY CAST(o.orderDate AS date)")
    List<Object[]> getAverageOrderValuePerDay(@Param("startDate") LocalDateTime startDate);
}
