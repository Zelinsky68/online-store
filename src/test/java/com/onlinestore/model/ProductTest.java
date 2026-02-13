package com.onlinestore.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

class ProductTest {
    
    private Product product;
    
    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(100.0);
        product.setQuantity(10);
    }
    
    @Test
    void testProductCreation() {
        assertThat(product).isNotNull();
        assertThat(product.getId()).isEqualTo(1L);
        assertThat(product.getName()).isEqualTo("Test Product");
        assertThat(product.getDescription()).isEqualTo("Test Description");
        assertThat(product.getPrice()).isEqualTo(100.0);
        assertThat(product.getQuantity()).isEqualTo(10);
    }
    
    @Test
    void testProductSettersAndGetters() {
        product.setName("Updated Product");
        product.setPrice(150.0);
        product.setQuantity(5);
        
        assertThat(product.getName()).isEqualTo("Updated Product");
        assertThat(product.getPrice()).isEqualTo(150.0);
        assertThat(product.getQuantity()).isEqualTo(5);
    }
    
    @Test
    void testProductConstructor() {
        Product productWithConstructor = new Product(
            "Constructor Product", 
            "Constructor Description", 
            200.0, 
            15
        );
        
        assertThat(productWithConstructor.getName()).isEqualTo("Constructor Product");
        assertThat(productWithConstructor.getDescription()).isEqualTo("Constructor Description");
        assertThat(productWithConstructor.getPrice()).isEqualTo(200.0);
        assertThat(productWithConstructor.getQuantity()).isEqualTo(15);
    }
    
    @Test
    void testDatesAreSet() {
        LocalDateTime now = LocalDateTime.now();
        product.setCreatedAt(now);
        product.setUpdatedAt(now);
        
        assertThat(product.getCreatedAt()).isEqualTo(now);
        assertThat(product.getUpdatedAt()).isEqualTo(now);
    }
    
    @Test
    void testToString() {
        String toString = product.toString();
        assertThat(toString).contains("id=1");
        assertThat(toString).contains("name='Test Product'");
        assertThat(toString).contains("price=100.0");
        assertThat(toString).contains("quantity=10");
    }
}
