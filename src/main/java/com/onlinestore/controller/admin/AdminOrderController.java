package com.onlinestore.controller.admin;

import com.onlinestore.dto.OrderDto;
import com.onlinestore.model.Order;
import com.onlinestore.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    @Autowired
    private OrderRepository orderRepository;

    // ============= GET METHODS =============

    @GetMapping
    @Transactional(readOnly = true)
    public List<OrderDto> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<OrderDto> getOrderById(@PathVariable Long id) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(convertToDto(order), HttpStatus.OK);
    }

    @GetMapping("/status/{status}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<OrderDto>> getOrdersByStatus(@PathVariable String status) {
        List<Order> orders = orderRepository.findByStatusOrderByOrderDateDesc(status);
        List<OrderDto> orderDtos = orders.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return new ResponseEntity<>(orderDtos, HttpStatus.OK);
    }

    @GetMapping("/period")
    @Transactional(readOnly = true)
    public ResponseEntity<List<OrderDto>> getOrdersByPeriod(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {
        List<Order> orders = orderRepository.findByOrderDateBetween(start, end);
        List<OrderDto> orderDtos = orders.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return new ResponseEntity<>(orderDtos, HttpStatus.OK);
    }

    // ============= STATISTICS =============

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getOrderStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Общее количество заказов
        long totalOrders = orderRepository.count();
        stats.put("totalOrders", totalOrders);
        
        // Заказы сегодня
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        long todayOrders = orderRepository.countByOrderDateBetween(startOfDay, endOfDay);
        stats.put("todayOrders", todayOrders);
        
        // Заказы за текущую неделю
        LocalDateTime startOfWeek = LocalDateTime.now().minusDays(7).withHour(0).withMinute(0).withSecond(0);
        long weekOrders = orderRepository.countByOrderDateAfter(startOfWeek);
        stats.put("weekOrders", weekOrders);
        
        // Заказы за текущий месяц
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        long monthOrders = orderRepository.countByOrderDateAfter(startOfMonth);
        stats.put("monthOrders", monthOrders);
        
        // Статистика по статусам
        List<Object[]> statusCounts = orderRepository.countOrdersByStatus();
        Map<String, Long> statusStats = new HashMap<>();
        for (Object[] row : statusCounts) {
            statusStats.put((String) row[0], (Long) row[1]);
        }
        stats.put("ordersByStatus", statusStats);
        
        // Средняя сумма заказа
        Double avgAmount = orderRepository.getAverageOrderAmount();
        stats.put("averageOrderAmount", avgAmount != null ? avgAmount : 0.0);
        
        // Общая выручка
        Double totalRevenue = orderRepository.getTotalRevenue();
        stats.put("totalRevenue", totalRevenue != null ? totalRevenue : 0.0);
        
        // Выручка сегодня
        Double todayRevenue = orderRepository.getRevenueBetweenDates(startOfDay, endOfDay);
        stats.put("todayRevenue", todayRevenue != null ? todayRevenue : 0.0);
        
        // Максимальная сумма заказа
        Double maxOrderAmount = orderRepository.getMaxOrderAmount();
        stats.put("maxOrderAmount", maxOrderAmount != null ? maxOrderAmount : 0.0);
        
        return new ResponseEntity<>(stats, HttpStatus.OK);
    }

    // ============= UPDATE METHODS =============

    @PatchMapping("/{id}/status")
    @Transactional
    public ResponseEntity<OrderDto> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);
        return new ResponseEntity<>(convertToDto(updatedOrder), HttpStatus.OK);
    }

    // ============= HELPER METHODS =============

    private OrderDto convertToDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setOrderDate(order.getOrderDate());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setShippingAddress(order.getShippingAddress());

        if (order.getUser() != null) {
            dto.setUserId(order.getUser().getId());
            dto.setUserName(order.getUser().getUsername());
            dto.setUserEmail(order.getUser().getEmail());
        }

        return dto;
    }
}
