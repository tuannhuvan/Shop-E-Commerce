package org.example.demofullstackspringboot.service;

import org.example.demofullstackspringboot.dto.CategoryCreateRequest;
import org.example.demofullstackspringboot.dto.CategoryResponse;

import java.util.List;

public interface CategoryService {
    List<CategoryResponse> getCategories();
    CategoryResponse getCategoryById(String id);
    CategoryResponse createCategory(CategoryCreateRequest request);
}
