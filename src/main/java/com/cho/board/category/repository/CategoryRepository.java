package com.cho.board.category.repository;

import com.cho.board.category.entity.Category;
import com.cho.board.category.entity.CategoryType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

}
