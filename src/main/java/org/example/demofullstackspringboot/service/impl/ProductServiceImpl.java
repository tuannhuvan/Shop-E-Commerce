package org.example.demofullstackspringboot.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.demofullstackspringboot.dto.*;
import org.example.demofullstackspringboot.entity.Category;
import org.example.demofullstackspringboot.entity.Product;
import org.example.demofullstackspringboot.exception.ResourceNotFoundException;
import org.example.demofullstackspringboot.repository.CategoryRepository;
import org.example.demofullstackspringboot.repository.ProductRepository;
import org.example.demofullstackspringboot.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public ProductPageResponse getProducts(String categoryId, BigDecimal minPrice, BigDecimal maxPrice, String keyword, Pageable pageable) {
        String cleanKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        String cleanCategoryId = (categoryId != null && !categoryId.trim().isEmpty()) ? categoryId.trim() : null;

        Page<Product> page = productRepository.findProductsWithFilters(cleanCategoryId, minPrice, maxPrice, cleanKeyword, pageable);
        List<ProductResponse> items = page.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        PaginationMetadata pagination = PaginationMetadata.builder()
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();

        return ProductPageResponse.builder()
                .data(items)
                .pagination(pagination)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(String id) {
        Product product = productRepository.findProductById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + id));
        return mapToResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + request.getCategoryId()));

        Product product = Product.builder()
                .id(UUID.randomUUID().toString())
                .category(category)
                .name(request.getName())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .createdAt(LocalDateTime.now())
                .build();

        return mapToResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(String id, ProductUpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + id));

        if (request.getName() != null && !request.getName().isBlank()) {
            product.setName(request.getName());
        }
        if (request.getCategoryId() != null && !request.getCategoryId().isBlank()) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + request.getCategoryId()));
            product.setCategory(category);
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getQuantity() != null) {
            product.setQuantity(request.getQuantity());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getImageUrl() != null) {
            product.setImageUrl(request.getImageUrl());
        }

        return mapToResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public void deleteProduct(String id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + id);
        }
        productRepository.deleteById(id);
    }

    public ProductResponse mapToResponse(Product product) {
        CategoryResponse categoryResponse = null;
        if (product.getCategory() != null) {
            categoryResponse = CategoryResponse.builder()
                    .id(product.getCategory().getId())
                    .name(product.getCategory().getName())
                    .slug(product.getCategory().getSlug())
                    .description(product.getCategory().getDescription())
                    .build();
        }

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .category(categoryResponse)
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .description(product.getDescription())
                .imageUrl(product.getImageUrl())
                .createdAt(product.getCreatedAt())
                .build();
    }
}
