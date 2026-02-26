package com.onlinestore.controller.admin;

import com.onlinestore.dto.OrderDto;
import com.onlinestore.model.Order;
import com.onlinestore.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    @Autowired
    private OrderRepository orderRepository;

    // Получить все заказы
    @GetMapping
    @Transactional(readOnly = true)
    public List<OrderDto> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // НОВЫЙ МЕТОД: Получить заказ по ID
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<OrderDto> getOrderById(@PathVariable Long id) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(convertToDto(order), HttpStatus.OK);
    }

    // НОВЫЙ МЕТОД: Обновить статус заказа
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

    // Конвертер Order -> OrderDto
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
