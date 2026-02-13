package com.onlinestore.controller;

import com.onlinestore.model.Product;
import com.onlinestore.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
public class SimpleController {

    @Autowired
    private ProductService productService;

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
        info.put("endpoints", java.util.Arrays.asList(
            "GET / - Главная страница",
            "GET /test - Тест API",
            "GET /health - Проверка здоровья",
            "GET /products - Все продукты",
            "POST /products - Создать продукт"
        ));
        return info;
    }

    // ========== PRODUCT ENDPOINTS ==========
    
    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts() {
        try {
            List<Product> products = productService.getAllProducts();
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        try {
            Product product = productService.getProductById(id);
            if (product != null) {
                return ResponseEntity.ok(product);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/products")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        try {
            // Автоматически устанавливаем даты
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            product.setCreatedAt(now);
            product.setUpdatedAt(now);
            
            Product savedProduct = productService.saveProduct(product);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/products/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam(required = false) String keyword) {
        try {
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @RequestBody Product updatedProduct) {
        try {
            Product existingProduct = productService.getProductById(id);
            if (existingProduct == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            
            // Обновляем поля
            existingProduct.setName(updatedProduct.getName());
            existingProduct.setDescription(updatedProduct.getDescription());
            existingProduct.setPrice(updatedProduct.getPrice());
            existingProduct.setQuantity(updatedProduct.getQuantity());
            existingProduct.setUpdatedAt(java.time.LocalDateTime.now());
            
            Product savedProduct = productService.saveProduct(existingProduct);
            return ResponseEntity.ok(savedProduct);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        try {
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
            Product product = productService.getProductById(id);
            if (product == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            
            int newQuantity = product.getQuantity() + quantity;
            if (newQuantity < 0) {
                newQuantity = 0;
            }
            
            product.setQuantity(newQuantity);
            product.setUpdatedAt(java.time.LocalDateTime.now());
            
            Product savedProduct = productService.saveProduct(product);
            return ResponseEntity.ok(savedProduct);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
