package com.cho.board.service.post;

import com.cho.board.controller.post.dtos.PostCreateRequest;
import com.cho.board.controller.post.dtos.PostUpdateRequest;
import com.cho.board.domain.category.Category;
import com.cho.board.domain.post.Post;
import com.cho.board.domain.user.User;
import com.cho.board.repository.category.CategoryRepository;
import com.cho.board.repository.post.PostRepository;
import com.cho.board.repository.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
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
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Category category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new EntityNotFoundException("Category not found"));

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
            .orElseThrow(() -> new EntityNotFoundException("Post not found with ID : " + id));
    }

    public Post update(Long postId, Long userId, PostUpdateRequest request) {
        Post post = findById(postId);

        if (!post.getAuthor().getId().equals(userId)) {
            throw new IllegalArgumentException("Only author can update the post");
        }

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
        }

        post.update(request.getTitle(), request.getContent(), category);

        return post;
    }

    public void delete(Long postId, Long userId) {
        Post post = findById(postId);

        if (!post.getAuthor().getId().equals(userId)) {
            throw new IllegalArgumentException("Only author can delete the post");
        }

        postRepository.delete(post);
    }

    public void increaseViewCount(Long postId) {
        Post post = findById(postId);
        post.increaseViewCount();
    }

}
