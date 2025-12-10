package com.cho.board.post.dtos;

import com.cho.board.post.entity.Post;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class PostListResponse extends PostBaseResponse {

    private String thumbnailPath;
    private Integer imageCount;

    public static PostListResponse from(Post post) {

        String thumbnail = post.getImages().isEmpty()
            ? null
            : post.getImages().get(0).getFilePath();

        return PostListResponse.builder()
            .id(post.getId())
            .title(post.getTitle())
            .authorName(post.getAuthor().getName())
            .categoryType(post.getCategory().getCategoryType())
            .createdAt(post.getCreatedAt())
            .viewCount(post.getViewCount())

            // 자식 클래스 필드
            .thumbnailPath(thumbnail)
            .imageCount(post.getImages().size())
            .build();
    }
}