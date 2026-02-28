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

    private Product product1;
    private Product product2;
    private User user;
    private Order order;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        product1 = new Product();
        product1.setId(1L);
        product1.setName("Test Product 1");
        product1.setDescription("Description 1");
        product1.setPrice(99.99);
        product1.setQuantity(10);
        product1.setCreatedAt(now);
        product1.setUpdatedAt(now);

        product2 = new Product();
        product2.setId(2L);
        product2.setName("Search Result Product");
        product2.setDescription("This product should be found by search");
        product2.setPrice(49.99);
        product2.setQuantity(5);
        product2.setCreatedAt(now);
        product2.setUpdatedAt(now);

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
        List<Product> products = Arrays.asList(product1, product2);
        when(productService.getAllProducts()).thenReturn(products);

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Product 1"));
    }

    @Test
    void testGetProductById() throws Exception {
        when(productService.getProductById(1L)).thenReturn(product1);

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Product 1"));
    }

    @Test
    void testGetProductById_NotFound() throws Exception {
        when(productService.getProductById(99L)).thenReturn(null);

        mockMvc.perform(get("/products/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testSearchProducts() throws Exception {
        // Настраиваем мок для getAllProducts (так как searchProducts использует его)
        List<Product> allProducts = Arrays.asList(product1, product2);
        when(productService.getAllProducts()).thenReturn(allProducts);

        // Выполняем поиск
        mockMvc.perform(get("/products/search")
                .param("keyword", "Search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].name").value("Search Result Product"));
        
        // Проверяем, что getAllProducts был вызван
        verify(productService, times(1)).getAllProducts();
    }

    @Test
    void testSearchProducts_EmptyKeyword() throws Exception {
        // Настраиваем мок для getAllProducts
        List<Product> allProducts = Arrays.asList(product1, product2);
        when(productService.getAllProducts()).thenReturn(allProducts);

        // Поиск с пустым ключевым словом должен вернуть все продукты
        mockMvc.perform(get("/products/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
        
        verify(productService, times(1)).getAllProducts();
    }

    @Test
    void testSearchProducts_NoResults() throws Exception {
        // Настраиваем мок для getAllProducts
        when(productService.getAllProducts()).thenReturn(List.of());

        // Поиск с ключевым словом должен вернуть пустой массив
        mockMvc.perform(get("/products/search")
                .param("keyword", "Nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
        
        verify(productService, times(1)).getAllProducts();
    }

    @Test
    void testGetStats() throws Exception {
        List<Product> products = Arrays.asList(product1, product2);
        when(productService.getAllProducts()).thenReturn(products);

        mockMvc.perform(get("/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProducts").value(2));
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
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
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
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        
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
        when(productRepository.save(any(Product.class))).thenReturn(product1);
        
        mockMvc.perform(patch("/api/orders/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }
}
