package com.cho.board.fixture;

import com.cho.board.category.entity.Category;
import com.cho.board.category.entity.CategoryType;
import java.util.ArrayList;
import java.util.List;

public class CategoryFixture {

    // 기본 카테고리 생성 (FREE 타입)
    public static Category createDefaultCategory() {
        return Category.builder()
            .categoryType(CategoryType.FREE)
            .description("자유게시판")
            .isActive(true)
            .build();
    }

    // 특정 타입의 카테고리 생성
    public static Category createCategory(CategoryType type) {
        String description = getDescriptionByType(type);
        return Category.builder()
            .categoryType(type)
            .description(description)
            .isActive(true)
            .build();
    }

    // 자유게시판 카테고리
    public static Category createFreeCategory() {
        return Category.builder()
            .categoryType(CategoryType.FREE)
            .description("자유게시판")
            .isActive(true)
            .build();
    }

    // 질문게시판 카테고리
    public static Category createQuestionCategory() {
        return Category.builder()
            .categoryType(CategoryType.QNA)
            .description("질문게시판")
            .isActive(true)
            .build();
    }

    // 여러 카테고리 생성
    public static List<Category> createMultipleCategories(int count) {
        List<Category> categories = new ArrayList<>();
        CategoryType[] types = CategoryType.values();

        for (int i = 0; i < count; i++) {
            CategoryType type = types[i % types.length];
            categories.add(createCategory(type));
        }

        return categories;
    }

    // CategoryType에 따른 설명 반환
    private static String getDescriptionByType(CategoryType type) {
        return switch (type) {
            case FREE -> "자유게시판";
            case NOTICE -> "공지사항";
            case QNA -> "질문게시판";
            case TECH -> "기술게시판";
        };
    }
}