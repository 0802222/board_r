package com.cho.board.post.service;

import com.cho.board.post.entity.Post;
import com.cho.board.post.repository.PostRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class N1PerformanceTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("N+1 문제 발생 - 기본 조회")
    void testN1Problem() {
        // given
        entityManager.clear(); // 영속성 컨텍스트 초기화

        // when
        long startTime = System.currentTimeMillis();
        List<Post> posts = postRepository.findAll();

        posts.forEach(post -> {
            post.getAuthor().getName(); // N+1 발생
            post.getCategory().getCategoryType(); // N+1 발생
        });

        long endTime = System.currentTimeMillis();

        // then
        System.out.println("기본 조회 시간: " + (endTime - startTime) + "ms");
    }

    @Test
    @DisplayName("@EntityGraph 로 해결")
    void testWithEntityGraph() {
        // given
        entityManager.clear();

        // when
        long startTime = System.currentTimeMillis();
        Page<Post> posts = postRepository.findAllWithUserAndCategory(Pageable.unpaged());

        posts.forEach(post -> {
            post.getAuthor().getName(); // 추가 쿼리 없음
            post.getCategory().getCategoryType(); // 추가 쿼리 없음
        });

        long endTime = System.currentTimeMillis();

        // then
        System.out.println("@EntityGraph 조회 시간: " + (endTime - startTime) + "ms");
    }

    @Test
    @DisplayName("Fetch Join 으로 해결")
    void testWithFetchJoin() {
        // given
        entityManager.clear();

        // when
        long startTime = System.currentTimeMillis();
        List<Post> posts = postRepository.findAllWithFetchJoin();

        posts.forEach(post -> {
            post.getAuthor().getName();
            post.getCategory().getCategoryType();
        });

        long endTime = System.currentTimeMillis();

        // then
        System.out.println("Fetch Join 조회 시간: " + (endTime - startTime) + "ms");
    }
}
