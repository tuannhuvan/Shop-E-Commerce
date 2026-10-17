package org.example.demofullstackspringboot.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.demofullstackspringboot.dto.CategoryCreateRequest;
import org.example.demofullstackspringboot.dto.CategoryResponse;
import org.example.demofullstackspringboot.entity.Category;
import org.example.demofullstackspringboot.exception.DuplicateResourceException;
import org.example.demofullstackspringboot.exception.ResourceNotFoundException;
import org.example.demofullstackspringboot.repository.CategoryRepository;
import org.example.demofullstackspringboot.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(String id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + id));
        return mapToResponse(category);
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryCreateRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Danh mục '" + request.getName() + "' đã tồn tại");
        }

        String slug = Category.toSlug(request.getName());
        Category category = Category.builder()
                .id(UUID.randomUUID().toString())
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .createdAt(LocalDateTime.now())
                .build();

        return mapToResponse(categoryRepository.save(category));
    }

    public CategoryResponse mapToResponse(Category category) {
        if (category == null) return null;
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .build();
    }
}
