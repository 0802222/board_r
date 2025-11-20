package com.cho.board.service.comment;


import com.cho.board.controller.comment.dtos.CommentResponse;
import com.cho.board.controller.comment.dtos.CommentUpdateRequest;
import com.cho.board.domain.comment.Comment;
import com.cho.board.controller.comment.dtos.CommentCreateRequest;
import com.cho.board.domain.post.Post;
import com.cho.board.domain.user.User;
import com.cho.board.repository.comment.CommentRepository;
import com.cho.board.repository.post.PostRepository;
import com.cho.board.repository.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // 특정 댓글 조회
    @Transactional(readOnly = true)
    public CommentResponse getCommentById(Long commentId) {

        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다."));
        return new CommentResponse(comment);
    }

    // 최 상위 댓글만 조회
    @Transactional(readOnly = true)
    public List<CommentResponse> getParentCommentsByPost(Long postId) {
        return commentRepository
            .findAllByPostId(postId)
            .stream()
            .filter(Comment::isParentComment)
            .map(CommentResponse::new)
            .collect(Collectors.toList());
    }

    // 모든 댓글 조회 (대댓글 포함)
    @Transactional(readOnly = true)
    public List<CommentResponse> getAllCommentsByPost(Long postId) {
        return commentRepository.findAllByPostId(postId)
            .stream()
            .map(CommentResponse::new)
            .collect(Collectors.toList());
    }

    // 댓글 작성
    public CommentResponse create(Long postId, Long authorId, CommentCreateRequest request) {

        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        User author = userRepository.findById(authorId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Comment parent = null;
        if (request.getParentId() != null) {
            parent = commentRepository.findById(request.getParentId())
                .orElseThrow(() -> new IllegalArgumentException("부모 댓글을 찾을 수 없습니다."));
        }

        Comment comment = Comment.builder()
            .content(request.getContent())
            .post(post)
            .author(author)
            .parent(parent)
            .build();

        Comment savedComment = commentRepository.save(comment);
        return new CommentResponse(savedComment);
    }

    // 댓글 수정
    public CommentResponse update(Long commentId, Long authorId, CommentUpdateRequest request) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다."));

        if (!comment.isAuthor(authorId)) {
            throw new IllegalArgumentException("댓글 작성자만 수정할 수 있습니다.");
        }

        if (comment.isDeleted()) {
            throw new IllegalArgumentException("삭제된 댓글은 수정할 수 없습니다.");
        }

        comment.updateContent(request.getContent());

        return new CommentResponse(comment);
    }

    public void delete(Long commentId, Long authorId) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다."));

        if (!comment.isAuthor(authorId)) {
            throw new IllegalArgumentException("댓글 작성자만 삭제할 수 있습니다.");
        }

        if (comment.isParentComment() && !comment.getChildren().isEmpty()) {
            // 최상위 댓글이고 대댓글이 있으면 소프트 삭제
            comment.delete();
        } else {
            // 대댓글이거나 자식이 없으면 실제 삭제
            commentRepository.delete(comment);
        }
    }
}
