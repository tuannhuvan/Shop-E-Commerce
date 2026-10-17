package org.example.demofullstackspringboot.service;

import org.example.demofullstackspringboot.dto.ProductCreateRequest;
import org.example.demofullstackspringboot.dto.ProductPageResponse;
import org.example.demofullstackspringboot.dto.ProductResponse;
import org.example.demofullstackspringboot.dto.ProductUpdateRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface ProductService {
    ProductPageResponse getProducts(String categoryId, BigDecimal minPrice, BigDecimal maxPrice, String keyword, Pageable pageable);
    ProductResponse getProductById(String id);
    ProductResponse createProduct(ProductCreateRequest request);
    ProductResponse updateProduct(String id, ProductUpdateRequest request);
    void deleteProduct(String id);
}
