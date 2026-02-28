package com.onlinestore.controller;

import com.onlinestore.model.Product;
import com.onlinestore.model.Order;
import com.onlinestore.model.OrderItem;
import com.onlinestore.model.User;
import com.onlinestore.service.ProductService;
import com.onlinestore.service.UserService;
import com.onlinestore.repository.OrderRepository;
import com.onlinestore.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.math.BigDecimal;

@RestController
public class SimpleController {

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SimpleController.class);

    @GetMapping("/")
    public String home() {
        return "Online Store API v1.0 - Java 11";
    }

    @GetMapping("/test")
    public String test() {
        return "API is working with PostgreSQL!";
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "OK");
        status.put("database", "PostgreSQL");
        status.put("java", "11");
        status.put("timestamp", System.currentTimeMillis());
        return status;
    }

    @GetMapping("/info")
    public Map<String, Object> getAppInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("application", "Online Store");
        info.put("version", "1.0.0");
        info.put("description", "REST API для онлайн-магазина");
        info.put("database", "PostgreSQL");
        info.put("javaVersion", "11");
        info.put("endpoints", Arrays.asList(
            "GET / - Главная страница",
            "GET /test - Тест API",
            "GET /health - Проверка здоровья",
            "GET /products - Все продукты",
            "POST /products - Создать продукт",
            "POST /api/orders - Создать заказ",
            "GET /api/orders/user/{userId} - Заказы пользователя",
            "GET /api/orders/{id} - Детали заказа",
            "PATCH /api/orders/{id}/cancel - Отменить заказ"
        ));
        return info;
    }

    // ========== PRODUCT ENDPOINTS ==========

    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts() {
        try {
            logger.info("Getting all products");
            List<Product> products = productService.getAllProducts();
            logger.info("Found {} products", products.size());
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            logger.error("Error getting products: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        try {
            logger.info("Getting product by id: {}", id);
            Product product = productService.getProductById(id);
            if (product != null) {
                return ResponseEntity.ok(product);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Error getting product by id {}: ", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/products")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        try {
            logger.info("Creating new product: {}", product.getName());
            LocalDateTime now = LocalDateTime.now();
            product.setCreatedAt(now);
            product.setUpdatedAt(now);

            Product savedProduct = productService.saveProduct(product);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
        } catch (Exception e) {
            logger.error("Error creating product: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        try {
            logger.info("Deleting product: {}", id);
            productService.deleteProduct(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error deleting product {}: ", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/products/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam(required = false) String keyword) {
        try {
            logger.info("Searching products with keyword: {}", keyword);
            List<Product> allProducts = productService.getAllProducts();

            if (keyword == null || keyword.trim().isEmpty()) {
                return ResponseEntity.ok(allProducts);
            }

            String searchTerm = keyword.toLowerCase().trim();
            List<Product> filtered = allProducts.stream()
                    .filter(p -> p.getName().toLowerCase().contains(searchTerm) ||
                                 (p.getDescription() != null &&
                                  p.getDescription().toLowerCase().contains(searchTerm)))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(filtered);
        } catch (Exception e) {
            logger.error("Error searching products: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @RequestBody Product updatedProduct) {
        try {
            logger.info("Updating product: {}", id);
            Product existingProduct = productService.getProductById(id);
            if (existingProduct == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            existingProduct.setName(updatedProduct.getName());
            existingProduct.setDescription(updatedProduct.getDescription());
            existingProduct.setPrice(updatedProduct.getPrice());
            existingProduct.setQuantity(updatedProduct.getQuantity());
            existingProduct.setUpdatedAt(LocalDateTime.now());

            Product savedProduct = productService.saveProduct(existingProduct);
            return ResponseEntity.ok(savedProduct);
        } catch (Exception e) {
            logger.error("Error updating product {}: ", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        try {
            logger.info("Getting product statistics");
            List<Product> products = productService.getAllProducts();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalProducts", products.size());

            double totalValue = 0;
            double totalPrice = 0;
            int inStock = 0;
            int outOfStock = 0;
            int totalQuantity = 0;

            for (Product p : products) {
                totalValue += p.getPrice() * p.getQuantity();
                totalPrice += p.getPrice();
                totalQuantity += p.getQuantity();
                if (p.getQuantity() > 0) {
                    inStock++;
                } else {
                    outOfStock++;
                }
            }

            double averagePrice = products.isEmpty() ? 0.0 : totalPrice / products.size();

            stats.put("totalValue", String.format("%.2f", totalValue));
            stats.put("averagePrice", String.format("%.2f", averagePrice));
            stats.put("totalQuantity", totalQuantity);
            stats.put("inStock", inStock);
            stats.put("outOfStock", outOfStock);
            stats.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error getting statistics: ", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/products/{id}/stock")
    public ResponseEntity<Product> updateStock(
            @PathVariable Long id,
            @RequestParam int quantity) {
        try {
            logger.info("Updating stock for product {}: quantity={}", id, quantity);
            Product product = productService.getProductById(id);
            if (product == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            int newQuantity = product.getQuantity() + quantity;
            if (newQuantity < 0) {
                newQuantity = 0;
            }

            product.setQuantity(newQuantity);
            product.setUpdatedAt(LocalDateTime.now());

            Product savedProduct = productService.saveProduct(product);
            return ResponseEntity.ok(savedProduct);
        } catch (Exception e) {
            logger.error("Error updating stock for product {}: ", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== ORDER ENDPOINTS FOR CUSTOMERS ==========

    @PostMapping("/api/orders")
    @Transactional
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> orderData) {
        try {
            logger.info("Creating order with data: {}", orderData);
            
            Long userId = Long.parseLong(orderData.get("userId").toString());
            String shippingAddress = (String) orderData.getOrDefault("shippingAddress", "");
            List<Map<String, Object>> items = (List<Map<String, Object>>) orderData.get("items");
            
            if (items == null || items.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Order must have at least one item"));
            }
            
            // Получаем пользователя
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found: " + userId));
            }
            
            // Создаем заказ
            Order order = new Order(user, shippingAddress);
            
            List<Map<String, Object>> orderItemsResponse = new ArrayList<>();
            
            for (Map<String, Object> itemData : items) {
                Long productId = Long.parseLong(itemData.get("productId").toString());
                int quantity = Integer.parseInt(itemData.get("quantity").toString());
                
                // Получаем товар
                Product product = productRepository.findById(productId).orElse(null);
                if (product == null) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Product not found: " + productId));
                }
                
                // Проверяем наличие
                if (product.getQuantity() < quantity) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "error", "Not enough stock for product: " + product.getName(),
                        "available", product.getQuantity(),
                        "requested", quantity
                    ));
                }
                
                // Создаем позицию заказа
                OrderItem item = new OrderItem();
                item.setProduct(product);
                item.setQuantity(quantity);
                item.setPrice(BigDecimal.valueOf(product.getPrice()));
                item.setOrder(order);
                
                order.addItem(item);
                
                // Уменьшаем количество на складе
                product.setQuantity(product.getQuantity() - quantity);
                product.setUpdatedAt(LocalDateTime.now());
                productRepository.save(product);
                
                Map<String, Object> itemResponse = new HashMap<>();
                itemResponse.put("productId", productId);
                itemResponse.put("productName", product.getName());
                itemResponse.put("quantity", quantity);
                itemResponse.put("price", product.getPrice());
                orderItemsResponse.add(itemResponse);
            }
            
            // Сохраняем заказ (каскадно сохранятся и items)
            Order savedOrder = orderRepository.save(order);
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", savedOrder.getId());
            response.put("totalAmount", savedOrder.getTotalAmount());
            response.put("status", savedOrder.getStatus());
            response.put("orderDate", savedOrder.getOrderDate());
            response.put("shippingAddress", savedOrder.getShippingAddress());
            response.put("items", orderItemsResponse);
            
            logger.info("Order created successfully with id: {}", savedOrder.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            logger.error("Error creating order: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create order: " + e.getMessage()));
        }
    }

    @GetMapping("/api/orders/user/{userId}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getOrdersByUser(@PathVariable Long userId) {
        try {
            logger.info("Getting orders for user: {}", userId);
            
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found: " + userId));
            }
            
            List<Order> orders = orderRepository.findByUser(user);
            
            // Сортируем и создаем DTO без доступа к ленивым коллекциям
            List<Map<String, Object>> simplifiedOrders = orders.stream()
                .sorted((a, b) -> b.getOrderDate().compareTo(a.getOrderDate()))
                .map(order -> {
                    Map<String, Object> simpleOrder = new HashMap<>();
                    simpleOrder.put("id", order.getId());
                    simpleOrder.put("totalAmount", order.getTotalAmount());
                    simpleOrder.put("status", order.getStatus());
                    simpleOrder.put("orderDate", order.getOrderDate());
                    simpleOrder.put("shippingAddress", order.getShippingAddress());
                    
                    // Считаем количество товаров безопасно
                    int itemCount = order.getItems() != null ? order.getItems().size() : 0;
                    simpleOrder.put("itemCount", itemCount);
                    
                    return simpleOrder;
                }).collect(Collectors.toList());
            
            logger.info("Found {} orders for user {}", simplifiedOrders.size(), userId);
            return ResponseEntity.ok(simplifiedOrders);
        } catch (Exception e) {
            logger.error("Error getting user orders: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get orders: " + e.getMessage()));
        }
    }

    @GetMapping("/api/orders/{orderId}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getOrderDetails(@PathVariable Long orderId) {
        try {
            logger.info("Getting order details for id: {}", orderId);
            
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order == null) {
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> detailedOrder = new HashMap<>();
            detailedOrder.put("id", order.getId());
            detailedOrder.put("totalAmount", order.getTotalAmount());
            detailedOrder.put("status", order.getStatus());
            detailedOrder.put("orderDate", order.getOrderDate());
            detailedOrder.put("shippingAddress", order.getShippingAddress());
            
            // Добавляем информацию о товарах
            List<Map<String, Object>> items = order.getItems().stream().map(item -> {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("productId", item.getProduct().getId());
                itemMap.put("productName", item.getProduct().getName());
                itemMap.put("quantity", item.getQuantity());
                itemMap.put("price", item.getPrice());
                return itemMap;
            }).collect(Collectors.toList());
            
            detailedOrder.put("items", items);
            
            return ResponseEntity.ok(detailedOrder);
        } catch (Exception e) {
            logger.error("Error getting order details: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get order details: " + e.getMessage()));
        }
    }

    @PatchMapping("/api/orders/{orderId}/cancel")
    @Transactional
    public ResponseEntity<?> cancelOrder(@PathVariable Long orderId) {
        try {
            logger.info("Cancelling order: {}", orderId);
            
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Проверяем можно ли отменить заказ
            if (!order.canBeCancelled()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Cannot cancel order with status: " + order.getStatus()
                ));
            }
            
            // Отменяем заказ
            order.cancel();
            order.setUpdatedAt(LocalDateTime.now());
            
            // Возвращаем товары на склад
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                product.setQuantity(product.getQuantity() + item.getQuantity());
                product.setUpdatedAt(LocalDateTime.now());
                productRepository.save(product);
            }
            
            orderRepository.save(order);
            
            logger.info("Order {} cancelled successfully", orderId);
            return ResponseEntity.ok(Map.of(
                "id", orderId,
                "status", "CANCELLED",
                "message", "Order cancelled successfully"
            ));
            
        } catch (IllegalStateException e) {
            logger.warn("Cannot cancel order {}: {}", orderId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error cancelling order: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to cancel order: " + e.getMessage()));
        }
    }

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

    @GetMapping("/test-json")
    public Map<String, String> testJson() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "OK");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return response;
    }
}
