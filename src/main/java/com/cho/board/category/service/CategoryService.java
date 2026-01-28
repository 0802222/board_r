package com.cho.board.category.service;

import com.cho.board.category.dtos.CategoryRequest;
import com.cho.board.category.dtos.CategoryResponse;
import com.cho.board.category.entity.Category;
import com.cho.board.category.repository.CategoryRepository;
import com.cho.board.global.exception.ErrorCode;
import com.cho.board.global.exception.ResourceNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponse create(CategoryRequest request) {

        Category category = Category.builder()
            .categoryType(request.getCategoryType())
            .description(request.getDescription())
            .isActive(true)
            .build();

        Category savedCategory = categoryRepository.save(category);
        return CategoryResponse.from(savedCategory);
    }

    @Transactional(readOnly = true)
    @Cacheable("categories")
    public List<CategoryResponse> getAllCategories() {
        // 캐시 미스 시에만 DB 조회
        return categoryRepository.findAll().stream()
            .map(CategoryResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public Category getCategory(Long categoryId) {

        return categoryRepository.findById(categoryId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CATEGORY_NOT_FOUND));
    }
}
