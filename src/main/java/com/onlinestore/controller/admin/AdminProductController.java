package com.onlinestore.controller.admin;

import com.onlinestore.dto.ProductDto;
import com.onlinestore.model.Product;
import com.onlinestore.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/products")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {

    @Autowired
    private ProductService productService;

    // ============= GET METHODS =============

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("asc") 
            ? Sort.by(sortBy).ascending() 
            : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> productPage = productService.getAllProductsPaginated(pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("products", productPage.getContent());
        response.put("currentPage", productPage.getNumber());
        response.put("totalItems", productPage.getTotalElements());
        response.put("totalPages", productPage.getTotalPages());
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        if (product != null) {
            return new ResponseEntity<>(product, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // ============= CREATE METHODS =============

    @PostMapping
    public ResponseEntity<Product> createProduct(@Valid @RequestBody ProductDto productDto) {
        Product product = convertToEntity(productDto);
        Product createdProduct = productService.saveProduct(product);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    @PostMapping("/batch")
    public ResponseEntity<List<Product>> createProducts(@Valid @RequestBody List<ProductDto> productDtos) {
        List<Product> products = productDtos.stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
        List<Product> createdProducts = productService.saveAllProducts(products);
        return new ResponseEntity<>(createdProducts, HttpStatus.CREATED);
    }

    // ============= UPDATE METHODS =============

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductDto productDto) {
        
        Product existingProduct = productService.getProductById(id);
        if (existingProduct == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        updateEntity(existingProduct, productDto);
        Product updatedProduct = productService.saveProduct(existingProduct);
        return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Product> partialUpdateProduct(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {
        
        Product product = productService.getProductById(id);
        if (product == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        applyPartialUpdates(product, updates);
        Product updatedProduct = productService.saveProduct(product);
        return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
    }

    @PatchMapping("/{id}/quantity")
    public ResponseEntity<Product> updateQuantity(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        
        Product product = productService.updateQuantity(id, quantity);
        return new ResponseEntity<>(product, HttpStatus.OK);
    }

    @PatchMapping("/{id}/price")
    public ResponseEntity<Product> updatePrice(
            @PathVariable Long id,
            @RequestParam BigDecimal price) {
        
        Product product = productService.updatePrice(id, price);
        return new ResponseEntity<>(product, HttpStatus.OK);
    }

    // ============= DELETE METHODS =============

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/batch")
    public ResponseEntity<Void> deleteProducts(@RequestBody List<Long> ids) {
        productService.deleteAllProducts(ids);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // ============= STATUS METHODS =============

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Product> activateProduct(@PathVariable Long id) {
        Product product = productService.activateProduct(id);
        return new ResponseEntity<>(product, HttpStatus.OK);
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Product> deactivateProduct(@PathVariable Long id) {
        Product product = productService.deactivateProduct(id);
        return new ResponseEntity<>(product, HttpStatus.OK);
    }

    // ============= BULK OPERATIONS =============

    @PostMapping("/bulk/update-prices")
    public ResponseEntity<Integer> bulkUpdatePrices(
            @RequestParam BigDecimal percentage,
            @RequestParam(required = false) String category) {
        
        int updatedCount = productService.bulkUpdatePrices(percentage, category);
        return new ResponseEntity<>(updatedCount, HttpStatus.OK);
    }

    // ============= STATISTICS =============

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getProductStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProducts", productService.getTotalCount());
        stats.put("totalValue", productService.getTotalInventoryValue());
        stats.put("averagePrice", productService.getAveragePrice());
        stats.put("outOfStock", productService.getOutOfStockCount());
        stats.put("lowStock", productService.getLowStockCount(5));
        
        return new ResponseEntity<>(stats, HttpStatus.OK);
    }

    // ============= HELPER METHODS =============

    private Product convertToEntity(ProductDto dto) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        if (dto.getPrice() != null) {
            product.setPrice(dto.getPrice().doubleValue());
        }
        product.setQuantity(dto.getQuantity());
        return product;
    }

    private void updateEntity(Product product, ProductDto dto) {
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        if (dto.getPrice() != null) {
            product.setPrice(dto.getPrice().doubleValue());
        }
        product.setQuantity(dto.getQuantity());
    }

    private void applyPartialUpdates(Product product, Map<String, Object> updates) {
        updates.forEach((key, value) -> {
            switch (key) {
                case "name":
                    if (value instanceof String) {
                        product.setName((String) value);
                    }
                    break;
                case "description":
                    if (value instanceof String) {
                        product.setDescription((String) value);
                    }
                    break;
                case "price":
                    if (value instanceof Number) {
                        product.setPrice(((Number) value).doubleValue());
                    }
                    break;
                case "quantity":
                    if (value instanceof Number) {
                        product.setQuantity(((Number) value).intValue());
                    }
                    break;
            }
        });
    }
}
