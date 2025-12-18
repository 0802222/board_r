package com.cho.board.post.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.cho.board.category.entity.Category;
import com.cho.board.category.entity.CategoryType;
import com.cho.board.category.repository.CategoryRepository;
import com.cho.board.global.exception.AccessDeniedException;
import com.cho.board.global.exception.ErrorCode;
import com.cho.board.global.exception.ResourceNotFoundException;
import com.cho.board.post.dtos.PostCreateRequest;
import com.cho.board.post.dtos.PostDetailResponse;
import com.cho.board.post.dtos.PostUpdateRequest;
import com.cho.board.post.entity.Post;
import com.cho.board.post.repository.PostRepository;
import com.cho.board.user.entity.Role;
import com.cho.board.user.entity.User;
import com.cho.board.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostService 단위 테스트")
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private PostService postService;

    // 테스트에서 사용할 데이터들
    private User user;
    private Post post;
    private Category category;
    private PostCreateRequest createRequest;
    private PostUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        // User 준비
        user = User.builder()
            .name("홍길동")
            .nickname("테스트유저")
            .email("test@example.com")
            .password("encodedPassword")
            .role(Role.USER)
            .build();

        ReflectionTestUtils.setField(user, "id", 1L);

        // Category 준비
        category = Category.builder()
            .categoryType(CategoryType.FREE)
            .build();
        ReflectionTestUtils.setField(category, "id", 1L);

        // Post 준비
        post = Post.builder()
            .title("제목")
            .content("내용")
            .author(user)
            .category(category)
            .build();
        ReflectionTestUtils.setField(post, "id", 1L);

        // CreateRequest 준비
        createRequest = PostCreateRequest.builder()
            .title("제목")
            .content("내용")
            .categoryId(1L)
            .build();

        // UpdateRequest 준비
        updateRequest = PostUpdateRequest.builder()
            .title("수정된 제목")
            .content("수정된 내용")
            .build();

        System.out.println("setUp 실행 됨! 테스트 데이터 준비 완료");
    }

    @Test
    @DisplayName("게시글 작성 성공")
    void create_성공() {
        // given
        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
        given(categoryRepository.findById(anyLong())).willReturn(Optional.of(category));
        given(postRepository.save(any(Post.class))).willReturn(post);

        // when
        PostDetailResponse result = postService.create(user.getEmail(), createRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("제목");
        assertThat(result.getContent()).isEqualTo("내용");

        // Mock 호출 확인
        then(userRepository).should().findByEmail("test@example.com");
        then(categoryRepository).should().findById(1L);
        then(postRepository).should().save(any(Post.class));
    }

    @Test
    @DisplayName("게시글 작성 실패 - 사용자 없음")
    void create_실패_사용자없음() {
        // given
        given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.create("notfound@test.com", createRequest))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

        // save 는 호출되지 않아야 함
        then(postRepository).should(never()).save(any(Post.class));
    }

    @Test
    @DisplayName("게시글 작성 실패 - 카테고리 없음")
    void create_실패_카테고리없음() {
        // given
        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
        given(categoryRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.create(user.getEmail(), createRequest))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CATEGORY_NOT_FOUND);

        // save 는 호출되지 않아야 함
        then(postRepository).should(never()).save(any(Post.class));
    }

    @Test
    @DisplayName("게시글 수정 성공")
    void update_성공() {
        // given
        given(postRepository.findById(anyLong())).willReturn(Optional.of(post));

        // when
        PostDetailResponse result = postService.update(1L, updateRequest, user.getEmail());

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("수정된 제목");
        assertThat(result.getContent()).isEqualTo("수정된 내용");

        then(postRepository).should().findById(1L);
    }

    @Test
    @DisplayName("게시글 수정 실패 - 게시글 없음")
    void update_실패_게시글없음() {
        // given
        given(postRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.update(999L, updateRequest, user.getEmail()))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.POST_NOT_FOUND);

        then(postRepository).should().findById(999L);
    }

    @Test
    @DisplayName("게시글 수정 실패 - 작성자가 아님")
    void update_실패_권한없음() {
        // given
        given(postRepository.findById(anyLong())).willReturn(Optional.of(post));

        // when & then
        assertThatThrownBy(() -> postService.update(1L, updateRequest, "other@test.com"))
            .isInstanceOf(AccessDeniedException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.POST_ACCESS_DENIED);

        then(postRepository).should().findById(1L);
    }

    @Test
    @DisplayName("게시글 삭제 성공")
    void delete_성공() {
        // given
        given(postRepository.findById(anyLong())).willReturn(Optional.of(post));

        // when
        postService.delete(1L, user.getEmail());

        // then
        then(postRepository).should().findById(1L);
        then(postRepository).should().delete(post);
    }

    @Test
    @DisplayName("게시글 삭제 실패 - 게시글 없음")
    void delete_실패_게시글없음() {
        // given
        given(postRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.delete(999L, user.getEmail()))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.POST_NOT_FOUND);

        then(postRepository).should().findById(999L);
        then(postRepository).should(never()).delete(any(Post.class));
    }

    @Test
    @DisplayName("게시글 삭제 실패 - 작성자가 아님")
    void delete_실패_권한없음() {
        // given
        given(postRepository.findById(anyLong())).willReturn(Optional.of(post));

        // when & then
        assertThatThrownBy(() -> postService.delete(1L, "other@test.com"))
            .isInstanceOf(AccessDeniedException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.POST_ACCESS_DENIED);

        then(postRepository).should().findById(1L);
        then(postRepository).should(never()).delete(any(Post.class));
    }

    @Test
    @DisplayName("게시글 조회 성공")
    void findById_성공() {
        // given
        given(postRepository.findById(anyLong())).willReturn(Optional.of(post));

        // when
        Post result = postService.findById(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("제목");

        then(postRepository).should().findById(1L);
    }

    @Test
    @DisplayName("게시글 조회 실패 - 존재하지 않음")
    void findById_실패() {
        // given
        given(postRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.findById(999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.POST_NOT_FOUND)
            .hasMessageContaining("게시글을 찾을 수 없습니다. ID: 999");

        then(postRepository).should().findById(999L);
    }
}