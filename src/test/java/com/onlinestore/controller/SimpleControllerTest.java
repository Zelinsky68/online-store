package com.onlinestore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlinestore.TestConfig;
import com.onlinestore.dto.ProductDto;
import com.onlinestore.model.Product;
import com.onlinestore.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SimpleController.class)
@ActiveProfiles("test")
@Import(TestConfig.class)
public class SimpleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    private ObjectMapper objectMapper = new ObjectMapper();
    private Product product;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(99.99);
        product.setQuantity(10);
        product.setCreatedAt(now);
        product.setUpdatedAt(now);
    }

    @Test
    void testHomeEndpoint() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Online Store")));
    }

    @Test
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"));
    }

    @Test
    void testGetAllProducts() throws Exception {
        List<Product> products = Arrays.asList(product);
        when(productService.getAllProducts()).thenReturn(products);

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Product"))
                .andExpect(jsonPath("$[0].price").value(99.99))
                .andExpect(jsonPath("$[0].quantity").value(10));
    }

    @Test
    void testGetProductById() throws Exception {
        when(productService.getProductById(1L)).thenReturn(product);

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    void testGetProductById_NotFound() throws Exception {
        when(productService.getProductById(99L)).thenReturn(null);

        mockMvc.perform(get("/products/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateProduct() throws Exception {
        Product newProduct = new Product();
        newProduct.setName("New Product");
        newProduct.setDescription("New Description");
        newProduct.setPrice(199.99);
        newProduct.setQuantity(20);

        Product createdProduct = new Product();
        createdProduct.setId(2L);
        createdProduct.setName("New Product");
        createdProduct.setDescription("New Description");
        createdProduct.setPrice(199.99);
        createdProduct.setQuantity(20);
        createdProduct.setCreatedAt(now);
        createdProduct.setUpdatedAt(now);

        when(productService.saveProduct(any(Product.class))).thenReturn(createdProduct);

        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newProduct)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("New Product"));
    }

    @Test
    void testUpdateProduct() throws Exception {
        Product updateProduct = new Product();
        updateProduct.setName("Updated Product");
        updateProduct.setDescription("Updated Description");
        updateProduct.setPrice(149.99);
        updateProduct.setQuantity(15);

        Product updatedProduct = new Product();
        updatedProduct.setId(1L);
        updatedProduct.setName("Updated Product");
        updatedProduct.setDescription("Updated Description");
        updatedProduct.setPrice(149.99);
        updatedProduct.setQuantity(15);
        updatedProduct.setUpdatedAt(now);

        when(productService.getProductById(1L)).thenReturn(product);
        when(productService.saveProduct(any(Product.class))).thenReturn(updatedProduct);

        mockMvc.perform(put("/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Product"))
                .andExpect(jsonPath("$.price").value(149.99))
                .andExpect(jsonPath("$.quantity").value(15));
    }

    @Test
    void testUpdateProduct_NotFound() throws Exception {
        Product updateProduct = new Product();
        updateProduct.setName("Updated Product");

        when(productService.getProductById(99L)).thenReturn(null);

        mockMvc.perform(put("/products/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateProduct)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteProduct() throws Exception {
        doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/products/1"))
                .andExpect(status().isNoContent());
        
        verify(productService).deleteProduct(1L);
    }

    @Test
    void testSearchProducts() throws Exception {
        List<Product> products = Arrays.asList(product);
        when(productService.getAllProducts()).thenReturn(products);

        mockMvc.perform(get("/products/search")
                .param("keyword", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Product"));
    }

    @Test
    void testGetStats() throws Exception {
        List<Product> products = Arrays.asList(product);
        when(productService.getAllProducts()).thenReturn(products);

        mockMvc.perform(get("/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProducts").value(1))
                .andExpect(jsonPath("$.totalQuantity").value(10));
    }

    @Test
    void testUpdateStock() throws Exception {
        Product updatedProduct = new Product();
        updatedProduct.setId(1L);
        updatedProduct.setQuantity(25);
        updatedProduct.setUpdatedAt(now);

        when(productService.getProductById(1L)).thenReturn(product);
        when(productService.saveProduct(any(Product.class))).thenReturn(updatedProduct);

        mockMvc.perform(post("/products/1/stock")
                .param("quantity", "15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.quantity").value(25));
    }

    @Test
    void testUpdateStock_Negative() throws Exception {
        Product updatedProduct = new Product();
        updatedProduct.setId(1L);
        updatedProduct.setQuantity(5);
        updatedProduct.setUpdatedAt(now);

        when(productService.getProductById(1L)).thenReturn(product);
        when(productService.saveProduct(any(Product.class))).thenReturn(updatedProduct);

        mockMvc.perform(post("/products/1/stock")
                .param("quantity", "-5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.quantity").value(5));
    }
}
