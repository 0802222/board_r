package com.cho.board.post.dtos;

import com.cho.board.post.entity.Post;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostDetailResponse {
    private Long id;
    private String title;
    private String content;
    private Long viewCount;

    private Long authorId;
    private String authorName;

    private Long categoryId;
    private String categoryType;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<PostImageResponse> images;

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
            .images(post.getImages().stream()
                .map(PostImageResponse::from)
                .collect(Collectors.toList()))
            .build();
    }
}
