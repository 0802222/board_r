package com.cho.board.post.service;

import static org.assertj.core.api.Assertions.assertThat;
import com.cho.board.category.entity.Category;
import com.cho.board.category.entity.CategoryType;
import com.cho.board.post.entity.Post;
import com.cho.board.user.entity.User;
import com.cho.board.post.repository.PostRepository;
import com.cho.board.user.repository.UserRepository;
import com.cho.board.category.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PostServiceNPlusOneTest {

    @Autowired
    private PostService postService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 생성
        User user = User.builder()
            .name("testuser")
            .nickname("testnick")
            .email("test@example.com")
            .password("password")
            .build();
        userRepository.save(user);

        Category category = Category.builder()
            .categoryType(CategoryType.FREE)
            .description("자유게시판")
            .build();
        categoryRepository.save(category);

        // 10 개의 게시글 생성
        for (int i = 1; i <= 10; i++) {
            Post post = Post.builder()
                .title("게시글 " + i)
                .content("내용 " + i)
                .author(user)
                .category(category)
                .build();
            postRepository.save(post);
        }
    }

    @Test
    @DisplayName("N+1 문제 발생 확인")
    void testNPlusOneProblem() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        System.out.println("========= 게시글 목록 조회 시작 =========");
        Page<Post> posts = postService.findAll(pageable);
        System.out.println("========= 게시글 목록 조회 완료 =========");

        // then
        assertThat(posts.getContent()).hasSize(10);

        // User 정보 접근 시 추가 쿼리 발생 확인
        System.out.println("========== User 정보 접근 시작 ==========");
        posts.getContent().forEach(post -> {
            System.out.println("게시글: " + post.getTitle() +
                ", 작성자: " + post.getAuthor().getName() +
                ", 카테고리: " + post.getCategory().getCategoryType());
        });
        System.out.println("========== User 정보 접근 완료 ==========\n");
    }

    @Test
    @DisplayName("N+1 문제로 인한 성능 저하 측정")
    void measurePerformanceWithNPlusOne() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        long startTime = System.currentTimeMillis();

        Page<Post> posts = postService.findAll(pageable);

        // User 와 Category 접근하여 실제 쿼리 발생
        posts.getContent().forEach(post -> {
            String username = post.getAuthor().getName();
            CategoryType categoryName = post.getCategory().getCategoryType();
        });

        long endTime = System.currentTimeMillis();

        // then
        long executionTime = endTime - startTime;
        System.out.println("\n===========================================");
        System.out.println("실행 시간: " + executionTime + "ms");
        System.out.println("예상 쿼리 수: 1(Post) + 10(User) + 10(Category) = 21개");
        System.out.println("===========================================\n");

        assertThat(posts.getContent()).hasSize(10);
    }

    @Test
    @DisplayName("단일 게시글 조회 시 N+1 문제 확인")
    void testSinglePostNPlusOne() {
        // given
        Pageable pageable = PageRequest.of(0, 1);
        Page<Post> posts = postService.findAll(pageable);
        Long postId = posts.getContent().get(0).getId();

        // when
        System.out.println("\n========== 단일 게시글 조회 시작 ==========");
        Post post = postService.findById(postId);
        System.out.println("========== 단일 게시글 조회 완료 ==========\n");

        System.out.println("========== 연관 엔티티 접근 시작 ==========");
        String username = post.getAuthor().getName();
        CategoryType categoryName = post.getCategory().getCategoryType();
        System.out.println("작성자: " + username + ", 카테고리: " + categoryName);
        System.out.println("========== 연관 엔티티 접근 완료 ==========\n");

        // then
        assertThat(post).isNotNull();
    }

}
