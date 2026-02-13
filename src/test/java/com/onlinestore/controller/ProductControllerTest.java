package com.onlinestore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlinestore.model.Product;
import com.onlinestore.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Arrays;
import java.util.List;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SimpleController.class)
@ActiveProfiles("test")  // Используем тестовый профиль
class ProductControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private ProductService productService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private Product product1;
    private Product product2;
    
    @BeforeEach
    void setUp() {
        product1 = new Product("Product 1", "Description 1", 100.0, 10);
        product1.setId(1L);
        
        product2 = new Product("Product 2", "Description 2", 200.0, 20);
        product2.setId(2L);
    }
    
    @Test
    void testHomeEndpoint() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Online Store API")));
    }
    
    @Test
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.database").value("PostgreSQL"));
    }
    
    @Test
    void testGetAllProducts() throws Exception {
        // Given - МОКИРУЕМ сервис, чтобы он возвращал только тестовые данные
        List<Product> products = Arrays.asList(product1, product2);
        when(productService.getAllProducts()).thenReturn(products);
        
        // When & Then
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Product 1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Product 2"));
        
        verify(productService, times(1)).getAllProducts();
    }
    
    @Test
    void testGetAllProducts_Empty() throws Exception {
        // Given
        when(productService.getAllProducts()).thenReturn(Arrays.asList());
        
        // When & Then
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        
        verify(productService, times(1)).getAllProducts();
    }
    
    @Test
    void testGetProductById() throws Exception {
        // Given
        when(productService.getProductById(1L)).thenReturn(product1);
        
        // When & Then
        mockMvc.perform(get("/products/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Product 1"))
                .andExpect(jsonPath("$.price").value(100.0))
                .andExpect(jsonPath("$.quantity").value(10));
        
        verify(productService, times(1)).getProductById(1L);
    }
    
    @Test
    void testGetProductById_NotFound() throws Exception {
        // Given
        when(productService.getProductById(999L)).thenReturn(null);
        
        // When & Then
        mockMvc.perform(get("/products/{id}", 999L))
                .andExpect(status().isNotFound());
        
        verify(productService, times(1)).getProductById(999L);
    }
    
    @Test
    void testCreateProduct() throws Exception {
        // Given
        Product newProduct = new Product("New Product", "New Description", 300.0, 30);
        Product savedProduct = new Product("New Product", "New Description", 300.0, 30);
        savedProduct.setId(3L);
        
        when(productService.saveProduct(any(Product.class))).thenReturn(savedProduct);
        
        // When & Then
        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newProduct)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("New Product"))
                .andExpect(jsonPath("$.price").value(300.0));
        
        verify(productService, times(1)).saveProduct(any(Product.class));
    }
    
    @Test
    void testDeleteProduct() throws Exception {
        // When & Then
        mockMvc.perform(delete("/products/{id}", 1L))
                .andExpect(status().isNoContent());
        
        verify(productService, times(1)).deleteProduct(1L);
    }
    
    @Test
    void testSearchProducts() throws Exception {
        // Given
        List<Product> products = Arrays.asList(product1);
        when(productService.getAllProducts()).thenReturn(products);
        
        // When & Then
        mockMvc.perform(get("/products/search")
                .param("keyword", "Product 1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Product 1"));
    }
    
    @Test
    void testUpdateProduct() throws Exception {
        // Given
        Product updatedProduct = new Product("Updated Product", "Updated Description", 150.0, 15);
        
        when(productService.getProductById(1L)).thenReturn(product1);
        when(productService.saveProduct(any(Product.class))).thenReturn(updatedProduct);
        
        // When & Then
        mockMvc.perform(put("/products/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Product"))
                .andExpect(jsonPath("$.price").value(150.0));
        
        verify(productService, times(1)).getProductById(1L);
        verify(productService, times(1)).saveProduct(any(Product.class));
    }
    
    @Test
    void testUpdateProduct_NotFound() throws Exception {
        // Given
        Product updatedProduct = new Product("Updated Product", "Updated Description", 150.0, 15);
        
        when(productService.getProductById(999L)).thenReturn(null);
        
        // When & Then
        mockMvc.perform(put("/products/{id}", 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedProduct)))
                .andExpect(status().isNotFound());
        
        verify(productService, times(1)).getProductById(999L);
        verify(productService, never()).saveProduct(any(Product.class));
    }
}
