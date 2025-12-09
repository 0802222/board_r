package com.cho.board.post.repository;

import com.cho.board.post.entity.PostImage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {

    // 게시글의 모든 이미지 조회 (순서대로)
    List<PostImage> findByPostIdOrderByDisplayOrder(Long postId);

    // 게시글의 이미지 개수 카운트
    long countByPostId(Long postId);
}
