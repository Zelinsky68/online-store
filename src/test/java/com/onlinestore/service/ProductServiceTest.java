package com.onlinestore.service;

import com.onlinestore.model.Product;
import com.onlinestore.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {
    
    @Mock
    private ProductRepository productRepository;
    
    @InjectMocks
    private ProductService productService;
    
    private Product product1;
    private Product product2;
    
    @BeforeEach
    void setUp() {
        product1 = new Product("Продукт 1", "Описание 1", 100.0, 10);
        product1.setId(1L);
        
        product2 = new Product("Продукт 2", "Описание 2", 200.0, 20);
        product2.setId(2L);
    }
    
    @Test
    void testGetAllProducts() {
        // Given
        List<Product> products = Arrays.asList(product1, product2);
        when(productRepository.findAll()).thenReturn(products);
        
        // When
        List<Product> result = productService.getAllProducts();
        
        // Then
        assertThat(result).hasSize(2);
        assertThat(result).contains(product1, product2);
        verify(productRepository, times(1)).findAll();
    }
    
    @Test
    void testGetProductById() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        
        // When
        Product result = productService.getProductById(1L);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Продукт 1");
        verify(productRepository, times(1)).findById(1L);
    }
    
    @Test
    void testGetProductByIdNotFound() {
        // Given
        when(productRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When
        Product result = productService.getProductById(999L);
        
        // Then
        assertThat(result).isNull();
        verify(productRepository, times(1)).findById(999L);
    }
    
    @Test
    void testSaveProduct() {
        // Given
        Product newProduct = new Product("Новый продукт", "Описание", 300.0, 30);
        when(productRepository.save(any(Product.class))).thenReturn(newProduct);
        
        // When
        Product result = productService.saveProduct(newProduct);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Новый продукт");
        assertThat(result.getPrice()).isEqualTo(300.0);
        verify(productRepository, times(1)).save(newProduct);
    }
    
    @Test
    void testDeleteProduct() {
        // When
        productService.deleteProduct(1L);
        
        // Then
        verify(productRepository, times(1)).deleteById(1L);
    }
}
