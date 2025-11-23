package com.cho.board.comment.controller;


import com.cho.board.comment.dtos.CommentCreateRequest;
import com.cho.board.comment.dtos.CommentResponse;
import com.cho.board.comment.dtos.CommentUpdateRequest;
import com.cho.board.comment.service.CommentService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponse> createComment(
        @PathVariable Long postId,
        @RequestParam Long userId,
        @Valid @RequestBody CommentCreateRequest request) {

        CommentResponse comment = commentService.create(postId, userId, request);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(comment);
    }

    @GetMapping
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long postId) {
        List<CommentResponse> comments = commentService.getAllCommentsByPost(postId);

        return ResponseEntity.ok(comments);
    }

    // 특정 댓글 조회
    @GetMapping("/{commentId}")
    public ResponseEntity<CommentResponse> getComment(
            @PathVariable Long postId,
            @PathVariable Long commentId) {
        CommentResponse comment = commentService.getCommentById(commentId);

        return ResponseEntity.ok(comment);
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestParam Long userId,
            @Valid @RequestBody CommentUpdateRequest request) {

        CommentResponse comment = commentService.update(commentId, userId, request);

        return ResponseEntity.ok(comment);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestParam Long userId) {

        commentService.delete(commentId, userId);

        return ResponseEntity.noContent().build();
    }
}
