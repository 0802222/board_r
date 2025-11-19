package com.cho.board.controller.post.dtos;

import com.cho.board.domain.category.CategoryType;
import com.cho.board.domain.post.Post;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostDetailResponse {
    private Long id;
    private String title;
    private String content;
    private int viewCount;

    private Long authorId;
    private String authorName;

    private Long categoryId;
    private String categoryType;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PostDetailResponse from(Post post) {
        return PostDetailResponse.builder()
            .id(post.getId())
            .title(post.getTitle())
            .content(post.getContent())
            .viewCount(post.getViewCount())
            .authorId(post.getAuthor().getId())
            .authorName(post.getAuthor().getName())
            .categoryId(post.getCategory().getId())
            .categoryType(post.getCategory().getCategoryType().getDescription())
            .createdAt(post.getCreatedAt())
            .updatedAt(post.getUpdatedAt())
            .build();
    }
}
