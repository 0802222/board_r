package com.cho.board.post.dtos;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostSearchCondition {

    private String title;
    private String content;
    private String author;
    private Long categoryId;

}
