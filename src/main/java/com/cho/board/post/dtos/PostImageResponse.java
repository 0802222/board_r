package com.cho.board.post.dtos;

import com.cho.board.post.entity.PostImage;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostImageResponse {

    private Long id;
    private String filePath;
    private String originalFilename;
    private Long fileSize;
    private Integer displayOrder;

    public static PostImageResponse from(PostImage image) {
        return PostImageResponse.builder()
            .id(image.getId())
            .filePath(image.getFilePath())
            .originalFilename(image.getOriginalFilename())
            .fileSize(image.getFileSize())
            .displayOrder(image.getDisplayOrder())
            .build();
    }
}
