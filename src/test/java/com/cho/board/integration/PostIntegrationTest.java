package com.cho.board.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cho.board.category.entity.Category;
import com.cho.board.category.repository.CategoryRepository;
import com.cho.board.config.TestSecurityConfig;
import com.cho.board.fixture.CategoryFixture;
import com.cho.board.fixture.PostFixture;
import com.cho.board.fixture.UserFixture;
import com.cho.board.post.entity.Post;
import com.cho.board.post.repository.PostRepository;
import com.cho.board.user.entity.User;
import com.cho.board.user.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(TestSecurityConfig.class)
class PostIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        postRepository.deleteAll();
        userRepository.deleteAll();
        categoryRepository.deleteAll();

        testUser = userRepository.save(UserFixture.createDefaultUser());
        testCategory = categoryRepository.save(CategoryFixture.createDefaultCategory());
    }

    @Test
    @DisplayName("게시글 작성 통합 테스트")
    void createPost_Integration() throws Exception {
        // given
        String jsonRequest = String.format("""
            {
                "title": "테스트 제목",
                "content": "테스트 내용",
                "categoryId": %d
            }
            """, testCategory.getId());

        // when & then
        mockMvc.perform(post("/posts")
                .param("email", testUser.getEmail())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
            .andDo(print())
            .andExpect(status().isCreated())

            // ApiResponse 구조 검증
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.error").doesNotExist())

            // PostDetailResponse 검증
            .andExpect(jsonPath("$.data.id").exists())
            .andExpect(jsonPath("$.data.title").value("테스트 제목"))
            .andExpect(jsonPath("$.data.content").value("테스트 내용"))
            .andExpect(jsonPath("$.data.authorName").value(testUser.getName()))
            .andExpect(jsonPath("$.data.categoryType").value(testCategory.getCategoryType().name()))
            .andExpect(jsonPath("$.data.viewCount").value(0))
            .andExpect(jsonPath("$.data.createdAt").exists())
            .andExpect(jsonPath("$.data.updatedAt").exists())
            .andExpect(jsonPath("$.data.images").isEmpty());

        // DB 검증
        List<Post> posts = postRepository.findAll();
        assertThat(posts).hasSize(1);
        Post savedPost = posts.get(0);
        assertThat(savedPost.getTitle()).isEqualTo("테스트 제목");
        assertThat(savedPost.getContent()).isEqualTo("테스트 내용");
        assertThat(savedPost.getAuthor().getId()).isEqualTo(testUser.getId());
        assertThat(savedPost.getViewCount()).isEqualTo(0);
        assertThat(savedPost.getUpdatedAt()).isNotNull();
        assertThat(savedPost.getUpdatedAt()).isEqualTo(savedPost.getCreatedAt());

    }

    @Test
    @DisplayName("게시글 작성 - Validation 실패 (제목 누락)")
    void createPost_ValidationFail_TitleBlank() throws Exception {
        // given
        String jsonRequest = String.format("""
            {
                "title": "",
                "content": "테스트 내용입니다.",
                "categoryId": %d
            }
            """, testCategory.getId());

        // when & then
        mockMvc.perform(post("/posts")
                .param("email", testUser.getEmail())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("게시글 작성 - Validation 실패 (카테고리 누락)")
    void createPost_ValidationFail_CategoryNull() throws Exception {
        // given
        String jsonRequest = """
            {
                "title": "테스트 제목",
                "content": "테스트 내용입니다."
            }
            """;

        // when & then
        mockMvc.perform(post("/posts")
                .param("email", testUser.getEmail())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("게시글 목록 조회 - 페이징 테스트")
    void getPosts_Paging() throws Exception {
        // given
        List<Post> posts = PostFixture.createMultiplePosts(15, testUser, testCategory);
        postRepository.saveAll(posts);

        // when & then - 첫 페이지 (10개)
        mockMvc.perform(get("/posts")
                .param("page", "0")
                .param("size", "10"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content.length()").value(10))
            .andExpect(jsonPath("$.data.page.totalElements").value(15))
            .andExpect(jsonPath("$.data.page.totalPages").value(2))
            .andExpect(jsonPath("$.data.page.number").value(0))
            .andExpect(jsonPath("$.data.page.size").value(10));

        // 두 번째 페이지 (5개)
        mockMvc.perform(get("/posts")
                .param("page", "1")
                .param("size", "10"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content.length()").value(5))
            .andExpect(jsonPath("$.data.page.number").value(1));
    }

    @Test
    @DisplayName("게시글 상세 조회 - 조회수 증가 확인")
    void getPost_ViewCountIncrement() throws Exception {
        // given
        Post post = postRepository.save(
            PostFixture.createPost("조회수 테스트", "내용", testUser, testCategory)
        );

        Long initialViewCount = post.getViewCount();

        // when & then
        mockMvc.perform(get("/posts/{id}", post.getId()))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.viewCount").value(initialViewCount + 1));

        // DB 검증
        Post updatedPost = postRepository.findById(post.getId())
            .orElseThrow(() -> new AssertionError("Post not found"));
        assertThat(updatedPost.getViewCount()).isEqualTo(initialViewCount + 1);
    }

    @Test
    @DisplayName("게시글 수정 통합 테스트")
    void updatePost_Integration() throws Exception {
        // given
        Post post = postRepository.save(
            PostFixture.createPost("원래 제목", "원래 내용", testUser, testCategory)
        );

        Thread.sleep(100);

        String jsonRequest = """
            {
                "title": "수정된 제목",
                "content": "수정된 내용"
            }
            """;

        // when & then
        mockMvc.perform(put("/posts/{id}", post.getId())
                .param("email", testUser.getEmail())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
            .andDo(print())
            .andExpect(status().isOk())

            // ApiResponse 구조 검증
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.error").doesNotExist())

            // 수정된 내용 검증
            .andExpect(jsonPath("$.data.title").value("수정된 제목"))
            .andExpect(jsonPath("$.data.content").value("수정된 내용"))
            .andExpect(jsonPath("$.data.updatedAt").exists())

            // updatedAt > createdAt 검증
            .andExpect(result -> {
                String json = result.getResponse().getContentAsString();
                JsonNode node = objectMapper.readTree(json);

                LocalDateTime createdAt = LocalDateTime.parse(
                    node.get("data").get("createdAt").asText()
                );
                LocalDateTime updatedAt = LocalDateTime.parse(
                    node.get("data").get("updatedAt").asText()
                );

                assertThat(updatedAt).isAfter(createdAt);
            });

        // DB 검증
        Post updatedPost = postRepository.findById(post.getId())
            .orElseThrow(() -> new AssertionError("Post not found"));

        // 변경된 필드
        assertThat(updatedPost.getTitle()).isEqualTo("수정된 제목");
        assertThat(updatedPost.getContent()).isEqualTo("수정된 내용");
        assertThat(updatedPost.getUpdatedAt()).isAfterOrEqualTo(post.getCreatedAt());

        // 변경되지 않은 필드
        assertThat(updatedPost.getId()).isEqualTo(post.getId());
        assertThat(updatedPost.getAuthor().getId()).isEqualTo(testUser.getId());
        assertThat(updatedPost.getCategory().getId()).isEqualTo(testCategory.getId());
        assertThat(updatedPost.getViewCount()).isEqualTo(post.getViewCount());

        // 타임스탬프 검증
        assertThat(updatedPost.getCreatedAt()).isEqualTo(post.getCreatedAt());
        assertThat(updatedPost.getUpdatedAt()).isAfterOrEqualTo(post.getCreatedAt());
    }

    @Test
    @DisplayName("게시글 부분 수정 - 제목만 수정")
    void updatePost_PartialUpdate() throws Exception {
        // given
        Post post = postRepository.save(
            PostFixture.createPost("원래 제목", "원래 내용", testUser, testCategory)
        );

        String jsonRequest = """
            {
                "title": "수정된 제목만"
            }
            """;

        // when & then
        mockMvc.perform(put("/posts/{id}", post.getId())
                .param("email", testUser.getEmail())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.title").value("수정된 제목만"))
            .andExpect(jsonPath("$.data.content").value("원래 내용"));
    }

    @Test
    @DisplayName("게시글 삭제 통합 테스트")
    void deletePost_Integration() throws Exception {
        // given
        Post post = postRepository.save(
            PostFixture.createPost("삭제될 게시글", "내용", testUser, testCategory)
        );

        // when & then
        mockMvc.perform(delete("/posts/{id}", post.getId())
                .param("email", testUser.getEmail()))
            .andDo(print())
            .andExpect(status().isNoContent());

        // DB 검증
        assertThat(postRepository.findById(post.getId())).isEmpty();
    }

    // TODO: 카테고리별 필터링 기능이 구현되면 활성화
    // @Test
    // @DisplayName("카테고리별 게시글 조회")
    // void getPostsByCategory() throws Exception {
    //     // given
    //     Category category2 = categoryRepository.save(CategoryFixture.createQuestionCategory());
    //
    //     // 카테고리1에 게시글
    //     postRepository.save(
    //         PostFixture.createPost("카테고리1 게시글", "내용1", testUser, testCategory)
    //     );
    //
    //     // 카테고리2에 게시글
    //     postRepository.save(
    //         PostFixture.createPost("카테고리2 게시글", "내용2", testUser, category2)
    //     );
    //
    //     // when & then
    //     mockMvc.perform(get("/posts")
    //             .param("categoryId", testCategory.getId().toString()))
    //         .andDo(print())
    //         .andExpect(status().isOk())
    //         .andExpect(jsonPath("$.content.length()").value(1));
    // }

    @Test
    @DisplayName("존재하지 않는 게시글 조회 시 404 반환")
    void getPost_NotFound() throws Exception {
        // when & then
        mockMvc.perform(get("/posts/{id}", 999L))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("게시글 수정 - Validation 실패 (제목 길이 초과)")
    void updatePost_ValidationFail_TitleTooLong() throws Exception {
        // given
        Post post = postRepository.save(PostFixture.createDefaultPost(testUser, testCategory));

        String longTitle = "a".repeat(101);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("title", longTitle);
        String jsonRequest = objectMapper.writeValueAsString(requestBody);

        // when & then
        mockMvc.perform(put("/posts/{id}", post.getId())
                .param("email", testUser.getEmail())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("게시글 수정 - Validation 실패 (내용 길이 초과)")
    void updatePost_ValidationFail_ContentTooLong() throws Exception {
        // given
        Post post = postRepository.save(PostFixture.createDefaultPost(testUser, testCategory));

        String longContent = "a".repeat(10001);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("content", longContent);
        String jsonRequest = objectMapper.writeValueAsString(requestBody);

        // when & then
        mockMvc.perform(put("/posts/{id}", post.getId())
                .param("email", testUser.getEmail())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("여러 유저의 게시글 조회 테스트")
    void getPostsByMultipleUsers() throws Exception {
        // given
        List<User> users = UserFixture.createMultipleUsers(3);
        userRepository.saveAll(users);

        // 각 유저마다 게시글 생성
        users.forEach(user -> {
            Post post = PostFixture.createPost(
                user.getName() + "의 게시글",
                "내용",
                user,
                testCategory
            );
            postRepository.save(post);
        });

        // when & then
        mockMvc.perform(get("/posts"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content.length()").value(3));
    }
}