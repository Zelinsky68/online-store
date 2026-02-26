package com.onlinestore.service;

import com.onlinestore.model.Product;
import com.onlinestore.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;  // Только одно объявление!

    // Получить все товары
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // Получить товар по ID
    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    // Поиск товаров
    public List<Product> searchProducts(String query) {
        return productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query);
    }

    // Получить товары по категории
    public List<Product> getProductsByCategory(String category) {
        // Если у вас есть поле category, раскомментируйте:
        // return productRepository.findByCategory(category);
        return productRepository.findAll(); // временно
    }

    // Получить товары в ценовом диапазоне
    public List<Product> getProductsByPriceRange(Double min, Double max) {
        return productRepository.findByPriceBetween(min, max);
    }

    // Сохранить товар
    public Product saveProduct(Product product) {
        if (product.getCreatedAt() == null) {
            product.setCreatedAt(LocalDateTime.now());
        }
        product.setUpdatedAt(LocalDateTime.now());
        return productRepository.save(product);
    }

    // Удалить товар
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    // ============= NEW METHODS FOR ADMIN =============

    // Пагинация
    public Page<Product> getAllProductsPaginated(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    // Сохранить несколько товаров
    @Transactional
    public List<Product> saveAllProducts(List<Product> products) {
        products.forEach(product -> {
            if (product.getCreatedAt() == null) {
                product.setCreatedAt(LocalDateTime.now());
            }
            product.setUpdatedAt(LocalDateTime.now());
        });
        return productRepository.saveAll(products);
    }

    // Обновить количество
    @Transactional
    public Product updateQuantity(Long id, Integer quantity) {
        Product product = getProductById(id);
        if (product == null) {
            throw new RuntimeException("Product not found with id: " + id);
        }
        product.setQuantity(quantity);
        product.setUpdatedAt(LocalDateTime.now());
        return productRepository.save(product);
    }

    // Обновить цену
    @Transactional
    public Product updatePrice(Long id, BigDecimal price) {
        Product product = getProductById(id);
        if (product == null) {
            throw new RuntimeException("Product not found with id: " + id);
        }
        product.setPrice(price.doubleValue());
        product.setUpdatedAt(LocalDateTime.now());
        return productRepository.save(product);
    }

    // Активировать товар
    @Transactional
    public Product activateProduct(Long id) {
        Product product = getProductById(id);
        if (product == null) {
            throw new RuntimeException("Product not found with id: " + id);
        }
        // Если есть поле isActive:
        // product.setIsActive(true);
        product.setUpdatedAt(LocalDateTime.now());
        return productRepository.save(product);
    }

    // Деактивировать товар
    @Transactional
    public Product deactivateProduct(Long id) {
        Product product = getProductById(id);
        if (product == null) {
            throw new RuntimeException("Product not found with id: " + id);
        }
        // Если есть поле isActive:
        // product.setIsActive(false);
        product.setUpdatedAt(LocalDateTime.now());
        return productRepository.save(product);
    }

    // Удалить несколько товаров
    @Transactional
    public void deleteAllProducts(List<Long> ids) {
        productRepository.deleteAllById(ids);
    }

    // Массовое обновление цен
    @Transactional
    public int bulkUpdatePrices(BigDecimal percentage, String category) {
        List<Product> products = productRepository.findAll();
        
        int count = 0;
        for (Product product : products) {
            double newPrice = product.getPrice() * (1 + percentage.doubleValue() / 100);
            product.setPrice(newPrice);
            product.setUpdatedAt(LocalDateTime.now());
            count++;
        }
        productRepository.saveAll(products);
        return count;
    }

    // ============= STATISTICS METHODS =============

    public long getTotalCount() {
        return productRepository.count();
    }

    public double getTotalInventoryValue() {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .mapToDouble(p -> p.getPrice() * p.getQuantity())
                .sum();
    }

    public double getAveragePrice() {
        return productRepository.findAll().stream()
                .mapToDouble(Product::getPrice)
                .average()
                .orElse(0.0);
    }

    public long getOutOfStockCount() {
        return productRepository.findByQuantity(0).size();
    }

    public long getLowStockCount(int threshold) {
        return productRepository.findByQuantityLessThan(threshold).size();
    }
}
