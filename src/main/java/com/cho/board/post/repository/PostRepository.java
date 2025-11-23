package com.cho.board.post.repository;

import com.cho.board.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

    // 게시글 조회 (카테고리)
    Page<Post> findByCategory_Id(Long categoryId, Pageable pageable);

    // 게시글 조회 (작성자)
    Page<Post> findByAuthor_Id(Long authorId, Pageable pageable);

    // 게시글 조회 (제목)
    Page<Post> findByTitleContaining(String keyword, Pageable pageable);
}
