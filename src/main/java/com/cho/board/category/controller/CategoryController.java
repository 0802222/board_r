package com.cho.board.category.controller;

import com.cho.board.category.dtos.CategoryRequest;
import com.cho.board.category.dtos.CategoryResponse;
import com.cho.board.category.entity.Category;
import com.cho.board.category.service.CategoryService;
import com.cho.board.global.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories() {
        List<CategoryResponse> categories = categoryService.getAllCategories();

        return ResponseEntity.ok(ApiResponse.success(categories, "전체카테고리 조회 성공"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategory(
        @PathVariable Long id) {
        Category category = categoryService.getCategory(id);
        return ResponseEntity.ok(ApiResponse.success(CategoryResponse.from(category)));
    }


    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
        @Valid @RequestBody CategoryRequest request) {

        CategoryResponse category = categoryService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }

}
