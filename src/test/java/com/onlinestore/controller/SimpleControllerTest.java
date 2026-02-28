package com.onlinestore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlinestore.TestConfig;
import com.onlinestore.dto.ProductDto;
import com.onlinestore.model.Product;
import com.onlinestore.model.User;
import com.onlinestore.model.Order;
import com.onlinestore.service.ProductService;
import com.onlinestore.service.UserService;
import com.onlinestore.repository.OrderRepository;
import com.onlinestore.repository.ProductRepository;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

    @MockBean
    private UserService userService;

    @MockBean
    private OrderRepository orderRepository;

    @MockBean
    private ProductRepository productRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Product product;
    private Product searchProduct;
    private User user;
    private Order order;
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

        searchProduct = new Product();
        searchProduct.setId(2L);
        searchProduct.setName("Search Result Product");
        searchProduct.setDescription("This is a search result");
        searchProduct.setPrice(49.99);
        searchProduct.setQuantity(5);
        searchProduct.setCreatedAt(now);
        searchProduct.setUpdatedAt(now);

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        order = new Order();
        order.setId(1L);
        order.setUser(user);
        order.setTotalAmount(99.99);
        order.setStatus("PENDING");
        order.setOrderDate(now);
    }

    @Test
    void testHomeEndpoint() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    @Test
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAllProducts() throws Exception {
        List<Product> products = Arrays.asList(product);
        when(productService.getAllProducts()).thenReturn(products);

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Product"));
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
    void testSearchProducts() throws Exception {
        // Создаем список продуктов для поиска
        List<Product> searchResults = Arrays.asList(searchProduct);
        
        // Настраиваем мок для возврата результатов поиска
        when(productService.searchProducts(anyString())).thenReturn(searchResults);

        mockMvc.perform(get("/products/search")
                .param("keyword", "Search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].name").value("Search Result Product"));
        
        // Проверяем, что метод searchProducts был вызван с правильным параметром
        verify(productService, times(1)).searchProducts("Search");
    }

    @Test
    void testSearchProducts_EmptyResult() throws Exception {
        // Настраиваем мок для возврата пустого списка
        when(productService.searchProducts(anyString())).thenReturn(List.of());

        mockMvc.perform(get("/products/search")
                .param("keyword", "Nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
        
        verify(productService, times(1)).searchProducts("Nonexistent");
    }

    @Test
    void testGetStats() throws Exception {
        List<Product> products = Arrays.asList(product);
        when(productService.getAllProducts()).thenReturn(products);

        mockMvc.perform(get("/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProducts").value(1));
    }

    @Test
    void testPing() throws Exception {
        mockMvc.perform(get("/ping"))
                .andExpect(status().isOk())
                .andExpect(content().string("pong"));
    }

    @Test
    void testTestJson() throws Exception {
        mockMvc.perform(get("/test-json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("OK"));
    }

    @Test
    void testCreateOrder() throws Exception {
        when(userService.getUserById(1L)).thenReturn(user);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        
        String orderJson = "{\"userId\":1,\"shippingAddress\":\"Test Address\",\"items\":[{\"productId\":1,\"quantity\":2}]}";
        
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(orderJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void testCreateOrder_UserNotFound() throws Exception {
        when(userService.getUserById(99L)).thenReturn(null);
        
        String orderJson = "{\"userId\":99,\"shippingAddress\":\"Test Address\",\"items\":[{\"productId\":1,\"quantity\":2}]}";
        
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(orderJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateOrder_ProductNotFound() throws Exception {
        when(userService.getUserById(1L)).thenReturn(user);
        when(productRepository.findById(99L)).thenReturn(Optional.empty());
        
        String orderJson = "{\"userId\":1,\"shippingAddress\":\"Test Address\",\"items\":[{\"productId\":99,\"quantity\":2}]}";
        
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(orderJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateOrder_InsufficientStock() throws Exception {
        when(userService.getUserById(1L)).thenReturn(user);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        
        String orderJson = "{\"userId\":1,\"shippingAddress\":\"Test Address\",\"items\":[{\"productId\":1,\"quantity\":100}]}";
        
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(orderJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetOrdersByUser() throws Exception {
        when(userService.getUserById(1L)).thenReturn(user);
        when(orderRepository.findByUser(user)).thenReturn(Arrays.asList(order));
        
        mockMvc.perform(get("/api/orders/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void testGetOrderDetails() throws Exception {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        
        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void testCancelOrder() throws Exception {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        
        mockMvc.perform(patch("/api/orders/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }
}
