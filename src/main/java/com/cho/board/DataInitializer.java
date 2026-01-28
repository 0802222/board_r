package com.cho.board;

import com.cho.board.category.dtos.CategoryRequest;
import com.cho.board.category.entity.CategoryType;
import com.cho.board.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

//@Profile("local") // application-local.properties 일때만
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    // 서버 켜지면 자동으로 카테고리 Data 가 생성되어서 별도설정없이 빠르게 테스트 환경을 구축할 수 있음
    // 이거 없으면 매번 DB에 Insert 해야됨

    private final CategoryService categoryService;

    @Override
    public void run(String... args) throws Exception {
        if (categoryService.getAllCategories().isEmpty()) {
            categoryService.create(
                CategoryRequest.builder()
                    .categoryType(CategoryType.NOTICE)
                    .description("공지사항")
                    .build()
            );

            categoryService.create(
                CategoryRequest.builder()
                    .categoryType(CategoryType.FREE)
                    .description("자유게시판")
                    .build()
            );

            categoryService.create(
                CategoryRequest.builder()
                    .categoryType(CategoryType.QNA)
                    .description("질문")
                    .build()
            );

            categoryService.create(
                CategoryRequest.builder()
                    .categoryType(CategoryType.TECH)
                    .description("기술")
                    .build()
            );
        }
    }
}
