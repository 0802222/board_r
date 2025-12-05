package com.cho.board.post.service;

import com.cho.board.post.dtos.PostCreateRequest;
import com.cho.board.post.dtos.PostDetailResponse;
import com.cho.board.post.dtos.PostListResponse;
import com.cho.board.post.dtos.PostSearchCondition;
import com.cho.board.post.dtos.PostUpdateRequest;
import com.cho.board.category.entity.Category;
import com.cho.board.post.entity.Post;
import com.cho.board.post.repository.PostRepositoryImpl;
import com.cho.board.user.entity.User;
import com.cho.board.global.exception.AccessDeniedException;
import com.cho.board.global.exception.ErrorCode;
import com.cho.board.global.exception.ResourceNotFoundException;
import com.cho.board.category.repository.CategoryRepository;
import com.cho.board.post.repository.PostRepository;
import com.cho.board.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public Post create(Long userId, PostCreateRequest request) {
        User author = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        Category category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CATEGORY_NOT_FOUND));

        Post post = Post.builder()
            .title(request.getTitle())
            .content(request.getContent())
            .author(author)
            .category(category)
            .build();

        return postRepository.save(post);
    }

    @Transactional(readOnly = true)
    public Page<Post> findAll(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Post findById(Long id) {
        return postRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.POST_NOT_FOUND, "게시글을 찾을 수 없습니다. ID: " + id));
    }

    public Post update(Long postId, Long userId, PostUpdateRequest request) {
        Post post = findById(postId);

        if (!post.isAuthor(userId)) {
            throw new AccessDeniedException(ErrorCode.POST_ACCESS_DENIED);
        }

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CATEGORY_NOT_FOUND));
        }

        post.update(request.getTitle(), request.getContent(), category);

        return post;
    }

    public void delete(Long postId, Long userId) {
        Post post = findById(postId);

        if (!post.getAuthor().getId().equals(userId)) {
            throw new AccessDeniedException(ErrorCode.POST_ACCESS_DENIED);
        }

        postRepository.delete(post);
    }

    public void increaseViewCount(Long postId) {
        Post post = findById(postId);
        post.increaseViewCount();
    }

    // 검색 메서드
    @Transactional(readOnly = true)
    public Page<PostListResponse> searchPosts(PostSearchCondition condition, Pageable pageable) {
        Page<Post> posts = postRepository.searchPosts(condition, pageable);
        return posts.map(PostListResponse::from);
    }

}
