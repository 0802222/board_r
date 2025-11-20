package com.cho.board.controller.comment.dtos;

import com.cho.board.domain.user.User;
import com.cho.board.domain.post.Post;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentUpdateRequest {

    private Long commentId;
    private Long parentId;
    private String content;
    private Post post;
    private User user;

}
