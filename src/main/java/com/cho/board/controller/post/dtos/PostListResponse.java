package com.cho.board.controller.post.dtos;

import com.cho.board.domain.post.Post;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostListResponse {

    private Long id;
    private String title;
    private int viewCount;
    private String authorName;
    private String categoryType;
    private LocalDateTime createdAt;

    public static PostListResponse from(Post post) {
        return PostListResponse.builder()
            .id(post.getId())
            .title(post.getTitle())
            .viewCount(post.getViewCount())
            .authorName(post.getAuthor().getName())
            .categoryType(post.getCategory().getCategoryType().getDescription())
            .createdAt(post.getCreatedAt())
            .build();
    }
}