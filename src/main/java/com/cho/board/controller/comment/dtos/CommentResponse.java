package com.cho.board.controller.comment.dtos;

import com.cho.board.domain.comment.Comment;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public class CommentResponse {

    private Long id;
    private String content;
    private String authorName;
    private Long authorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean deleted;

    private List<CommentResponse> children = new ArrayList<>();

    public CommentResponse(Comment comment) {
        this.id = comment.getId();
        this.content = comment.isDeleted() ? "삭제된 댓글입니다." : comment.getContent();
        this.authorName = comment.getAuthor().getName();
        this.authorId = comment.getAuthor().getId();
        this.createdAt = comment.getCreatedAt();
        this.updatedAt = comment.getUpdatedAt();
        this.deleted = comment.isDeleted();

        if (comment.isParentComment()) {
            this.children = comment.getChildren().stream()
                .map(CommentResponse::new)
                .collect(Collectors.toList());
        }
    }
}
