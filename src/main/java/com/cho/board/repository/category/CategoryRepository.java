package com.cho.board.repository.category;

import com.cho.board.domain.category.Category;
import com.cho.board.domain.category.CategoryType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // 조회 (카테고리 타입)
    Optional<Category> findByCategoryType(CategoryType categoryType);

    // 조회 (활성 카테고리만)
    List<Category> findByIsActiveTrue();

}
