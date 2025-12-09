package com.cho.board.post.dtos;

import com.cho.board.post.entity.Post;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostListResponse {

    private Long id;
    private String title;
    private Long viewCount;
    private String authorName;
    private String categoryType;
    private LocalDateTime createdAt;
    private String thumbnailPath;
    private Integer imageCount;

    public static PostListResponse from(Post post) {

        String thumbnail = post.getImages().isEmpty()
            ? null
            : post.getImages().get(0).getFilePath();

        return PostListResponse.builder()
            .id(post.getId())
            .title(post.getTitle())
            .viewCount(post.getViewCount())
            .authorName(post.getAuthor().getName())
            .categoryType(post.getCategory().getCategoryType().getDescription())
            .createdAt(post.getCreatedAt())
            .thumbnailPath(thumbnail)
            .imageCount(post.getImages().size())
            .build();
    }
}