package com.cho.board.service.category;

import com.cho.board.domain.category.Category;
import com.cho.board.domain.category.CategoryType;
import com.cho.board.repository.category.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public Category create(CategoryType categoryType, String description) {
        Category category = Category.builder()
            .categoryType(categoryType)
            .description(description)
            .isActive(true)
            .build();
        return categoryRepository.save(category);
    }

    @Transactional(readOnly = true)
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Category findById(Long id) {
        return categoryRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Category not found"));
    }
}
