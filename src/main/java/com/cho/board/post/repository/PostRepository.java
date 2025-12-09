package com.cho.board.post.repository;

import com.cho.board.post.entity.Post;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

    // 게시글 조회 (카테고리)
    Page<Post> findByCategory_Id(Long categoryId, Pageable pageable);

    // 게시글 조회 (작성자)
    Page<Post> findByAuthor_Id(Long authorId, Pageable pageable);

    // 게시글 조회 (제목)
    Page<Post> findByTitleContaining(String keyword, Pageable pageable);


    // N + 1 문제 해결 : @Entity Graph 사용
    // User, Category 를 LEFT JOIN 으로 한 번에 조회
    @EntityGraph(attributePaths = {"author", "category"})
    @Query("SELECT p FROM Post p")
    Page<Post> findAllWithUserAndCategory(Pageable pageable);

    @EntityGraph(attributePaths = {"author", "category"})
    Optional<Post> findWithUserAndCategoryById(Long id);


    // N+1 문제 해결: Fetch Join 사용
    // JPQL 로 직접 JOIN 제어, 복잡한 조건 추가 가능
    @Query("SELECT p FROM Post p " +
        "LEFT JOIN FETCH p.author " +
        "LEFT JOIN FETCH p.category " +
        "ORDER BY p.createdAt DESC")
    List<Post> findAllWithFetchJoin();

    @Query("SELECT p FROM Post p " +
        "LEFT JOIN FETCH p.author " +
        "LEFT JOIN FETCH p.category " +
        "WHERE p.id = :id")
    Optional<Post> findByIdWithFetchJoin(@Param("id") Long id);;
}
