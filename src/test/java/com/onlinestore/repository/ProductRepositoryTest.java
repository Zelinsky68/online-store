package com.onlinestore.repository;

import com.onlinestore.model.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")  // Используем тестовый профиль с H2
class ProductRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Test
    void testSaveProduct() {
        // Given
        Product product = new Product("Test Product", "Test Description", 100.0, 10);
        
        // When
        Product saved = productRepository.save(product);
        
        // Then
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Test Product");
        assertThat(saved.getPrice()).isEqualTo(100.0);
    }
    
    @Test
    void testFindById() {
        // Given
        Product product = new Product("Test Product", "Test Description", 100.0, 10);
        Product saved = entityManager.persist(product);
        entityManager.flush();
        
        // When
        Optional<Product> found = productRepository.findById(saved.getId());
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Product");
    }
    
    @Test
    void testFindAll() {
        // Given
        Product product1 = new Product("Product 1", "Description 1", 100.0, 10);
        Product product2 = new Product("Product 2", "Description 2", 200.0, 20);
        
        entityManager.persist(product1);
        entityManager.persist(product2);
        entityManager.flush();
        
        // When
        List<Product> products = productRepository.findAll();
        
        // Then - ОЖИДАЕМ 2 продукта (те которые создали в тесте)
        assertThat(products).hasSize(2);
        assertThat(products).extracting(Product::getName)
                .containsExactlyInAnyOrder("Product 1", "Product 2");
    }
    
    @Test
    void testDeleteById() {
        // Given
        Product product = new Product("Test Product", "Test Description", 100.0, 10);
        Product saved = entityManager.persist(product);
        entityManager.flush();
        
        // When
        productRepository.deleteById(saved.getId());
        
        // Then
        Optional<Product> found = productRepository.findById(saved.getId());
        assertThat(found).isEmpty();
    }
}
