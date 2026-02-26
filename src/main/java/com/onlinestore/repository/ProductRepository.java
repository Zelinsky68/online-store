package com.onlinestore.repository;

import com.onlinestore.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    // Поиск по названию (частичное совпадение, без учета регистра)
    List<Product> findByNameContainingIgnoreCase(String name);
    
    // Поиск по названию или описанию
    List<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
        String name, String description);
    
    // Поиск по цене
    List<Product> findByPriceBetween(Double minPrice, Double maxPrice);
    
    // Товары с остатком больше 0 (в наличии)
    List<Product> findByQuantityGreaterThan(Integer quantity);
    
    // Сортировка по цене
    List<Product> findAllByOrderByPriceAsc();
    List<Product> findAllByOrderByPriceDesc();
    
    // Поиск по количеству
    List<Product> findByQuantity(Integer quantity);
    
    // Товары с остатком меньше порога
    List<Product> findByQuantityLessThan(Integer threshold);
    
    // Поиск по категории (если добавите поле category)
    // List<Product> findByCategory(String category);
}
