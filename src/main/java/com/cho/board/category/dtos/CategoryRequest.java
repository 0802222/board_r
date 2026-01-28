package com.cho.board.category.dtos;

import com.cho.board.category.entity.CategoryType;
import com.cho.board.global.validation.NoProfanity;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {

    @NotNull(message = "카테고리 선택은 필수입니다.")
    private CategoryType categoryType;

    @NoProfanity // custom annotation
    private String description;

}

