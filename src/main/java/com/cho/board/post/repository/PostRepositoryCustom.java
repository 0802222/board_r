package com.cho.board.post.repository;

import com.cho.board.post.dtos.PostSearchCondition;
import com.cho.board.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostRepositoryCustom {

    Page<Post> searchPosts(PostSearchCondition condition, Pageable pageable);

}
