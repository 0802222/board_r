package com.cho.board.category.dtos;

import com.cho.board.category.entity.Category;
import com.cho.board.category.entity.CategoryType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CategoryResponse {

    private Long id;
    private CategoryType categoryType;
    private String description;
    private Boolean isActive;

    public static CategoryResponse from(Category category) {
        return CategoryResponse.builder()
            .id(category.getId())
            .categoryType(category.getCategoryType())
            .description(category.getDescription())
            .isActive(category.getIsActive())
            .build();
    }
}
