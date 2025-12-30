package com.cho.board.post.dtos;

import com.cho.board.category.entity.CategoryType;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public abstract class PostBaseResponse {

    private Long id;
    private String title;
    private String authorName;
    private CategoryType categoryType;
    private LocalDateTime createdAt;
    private Long viewCount;

}
