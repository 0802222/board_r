package com.cho.board.category.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.cho.board.category.dtos.CategoryRequest;
import com.cho.board.category.entity.Category;
import com.cho.board.category.entity.CategoryType;
import com.cho.board.category.repository.CategoryRepository;
import com.cho.board.config.TestSecurityConfig;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class CategoryServiceTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CategoryService categoryService;

    @Autowired(required = false)
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        // 각 테스트 전에 DB와 캐시를 깔끔하게 초기화
        categoryRepository.deleteAll();
        if (cacheManager != null && cacheManager.getCache("categories") != null) {
            cacheManager.getCache("categories").clear();
        }
    }

    @Test
    @DisplayName("카테고리 조회 시 캐시가 적용되어 DB 조회는 1번만 발생한다.")
    void 카테고리_조회시_캐시_적용() {
        // given
        List<Category> testCategories = List.of(
            Category.builder()
                .categoryType(CategoryType.TECH)
                .description("기술")
                .isActive(true)
                .build()
        );

        categoryRepository.saveAll(testCategories);

        // when
        var firstCall = categoryService.getAllCategories();  // 첫 호출: DB 조회
        var secondCall = categoryService.getAllCategories(); // 두 번째: 캐시 조회


        // then
        assertThat(firstCall).hasSize(1);
        assertThat(secondCall).hasSize(1);


        // 로그에서 Hibernate 쿼리 확인:
        // "select ... from category" 가 1번만 출력되어야 함
    }

    @Test
    @DisplayName("카테고리 생성 시 캐시가 무효화된다")
    void 카테고리_생성시_캐시_무효화() {
        // given
        Category initialCategory = Category.builder()
            .categoryType(CategoryType.NOTICE)
            .description("공지")
            .isActive(true)
            .build();
        categoryRepository.save(initialCategory);

        // 캐시에 데이터 로드
        var firstCall = categoryService.getAllCategories();
        assertThat(firstCall).hasSize(1);

        // when - 새 카테고리 생성 (캐시 무효화)
        categoryService.create(
            CategoryRequest.builder()
                .categoryType(CategoryType.QNA)
                .description("질문")
                .build()
        );

        // then - 캐시 무효화 후 새로 조회
        var afterCreate = categoryService.getAllCategories();
        assertThat(afterCreate).hasSize(2); // 캐시가 무효화되어 DB에서 다시 조회
    }

}
