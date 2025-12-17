package com.cho.board.post.dtos;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class PostUpdateRequest {

    @Size(min = 1, max = 100, message = "제목은 1~100자여야 합니다.")
    private String title;

    @Size(min = 1, max = 10000, message = "내용은 1~10000자여야 합니다.")
    private String content;

    private Long categoryId;

}
