package com.cho.board.post.controller;

import com.cho.board.global.response.ApiResponse;
import com.cho.board.post.dtos.PostCreateRequest;
import com.cho.board.post.dtos.PostDetailResponse;
import com.cho.board.post.dtos.PostListResponse;
import com.cho.board.post.dtos.PostSearchCondition;
import com.cho.board.post.dtos.PostUpdateRequest;
import com.cho.board.post.entity.Post;
import com.cho.board.post.service.PostService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PostDetailResponse>> create(
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestBody @Valid PostCreateRequest request
    ) {
        String email = userDetails.getUsername();
        PostDetailResponse response = postService.create(email, request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PostListResponse>>> findAll(
        @PageableDefault(size = 20, sort = "createdAt", direction = Direction.DESC)
        Pageable pageable
    ) {
        Page<PostListResponse> posts = postService.findAll(pageable)
            .map(PostListResponse::from);
        return ResponseEntity.ok(ApiResponse.success(posts));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> findById(@PathVariable Long id) {
        Post post = postService.findByIdWithViewCount(id);
        return ResponseEntity.ok(ApiResponse.success(PostDetailResponse.from(post)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PostDetailResponse>> update(
        @PathVariable Long id,
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestBody @Valid PostUpdateRequest request
    ) {
        String email = userDetails.getUsername();
        PostDetailResponse response = postService.update(id, request, email);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> delete(
        @PathVariable Long id,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();
        postService.delete(id, email);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<PostListResponse>>> searchPosts(
        @ModelAttribute PostSearchCondition condition,
        Pageable pageable
    ) {
        Page<PostListResponse> result = postService.searchPosts(condition, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // N + 1 문제 해결 : @Entity Graph 사용
    // User, Category 를 LEFT JOIN 으로 한 번에 조회
    @GetMapping("/optimized")
    public ResponseEntity<ApiResponse<Page<PostListResponse>>> getPostsOptimized(
        @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(postService.getPostsOptimized(pageable)));
    }

    @GetMapping("/{id}/optimized")
    public ResponseEntity<ApiResponse<PostDetailResponse>> getPostByIdOptimized(
        @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(postService.getPostByIdOptimized(id)));
    }

    // N+1 문제 해결: Fetch Join 사용
    // JPQL 로 직접 JOIN 제어, 복잡한 조건 추가 가능
    @GetMapping("/fetch-join")
    public ResponseEntity<ApiResponse<List<PostListResponse>>> getPostsWithFetchJoin() {
        List<PostListResponse> posts = postService.getPostsWithFetchJoin();
        return ResponseEntity.ok(ApiResponse.success(posts, "게시글 목록 조회 성공"));
    }

    @GetMapping("/{id}/fetch-join")
    public ResponseEntity<ApiResponse<PostDetailResponse>> getPostByIdWithFetchJoin(
        @PathVariable Long id) {
        PostDetailResponse post = postService.getPostByIdWithFetchJoin(id);
        return ResponseEntity.ok(ApiResponse.success(post, "게시글 조회 성공"));
    }

}
