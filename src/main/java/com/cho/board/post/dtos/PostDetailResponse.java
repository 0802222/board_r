package com.cho.board.post.dtos;

import com.cho.board.post.entity.Post;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class PostDetailResponse extends PostBaseResponse {

    private String content;
    private Long authorId;
    private Long categoryId;
    private LocalDateTime updatedAt;
    private List<PostImageResponse> images;

    public static PostDetailResponse from(Post post) {
        return PostDetailResponse.builder()
            .id(post.getId())
            .title(post.getTitle())
            .authorName(post.getAuthor().getName())
            .categoryType(post.getCategory().getCategoryType().getDescription())
            .createdAt(post.getCreatedAt())
            .viewCount(post.getViewCount())

            // 자식 클래스 필드
            .content(post.getContent())
            .authorId(post.getAuthor().getId())
            .categoryId(post.getCategory().getId())
            .updatedAt(post.getUpdatedAt())
            .images(post.getImages().stream()
                .map(PostImageResponse::from)
                .collect(Collectors.toList()))
            .build();
    }
}
