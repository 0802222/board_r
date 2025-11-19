package com.cho.board.controller.post;

import com.cho.board.controller.post.dtos.PostCreateRequest;
import com.cho.board.controller.post.dtos.PostDetailResponse;
import com.cho.board.controller.post.dtos.PostListResponse;
import com.cho.board.controller.post.dtos.PostUpdateRequest;
import com.cho.board.domain.post.Post;
import com.cho.board.service.post.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
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
@RequestMapping("posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostDetailResponse> create(
        @RequestParam Long userId,
        @RequestBody @Valid PostCreateRequest request
    ) {
        Post post = postService.create(userId, request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(PostDetailResponse.from(post));
    }

    @GetMapping
    public ResponseEntity<Page<PostListResponse>> findAll(
        @PageableDefault(size = 20, sort = "createdAt", direction = Direction.DESC)
        Pageable pageable
    ) {
        Page<PostListResponse> posts = postService.findAll(pageable)
            .map(PostListResponse::from);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDetailResponse> findById(@PathVariable Long id) {
        postService.increaseViewCount(id);

        Post post = postService.findById(id);
        return ResponseEntity.ok(PostDetailResponse.from(post));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostDetailResponse> update(
        @PathVariable Long id,
        @RequestParam Long userId,
        @RequestBody @Valid PostUpdateRequest request
    ) {
        Post post = postService.update(id, userId, request);
        return ResponseEntity.ok(PostDetailResponse.from(post));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
        @PathVariable Long id,
        @RequestParam Long userId
    ) {
        postService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
}
