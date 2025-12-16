package com.cho.board.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cho.board.config.TestSecurityConfig;
import com.cho.board.fixture.UserFixture;
import com.cho.board.user.dtos.UserCreateRequest;
import com.cho.board.user.dtos.UserUpdateRequest;
import com.cho.board.user.entity.Role;
import com.cho.board.user.entity.User;
import com.cho.board.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
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
class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("회원가입 통합 테스트")
    void createUser_Integration() throws Exception {
        // given
        UserCreateRequest request = new UserCreateRequest(
            "홍길동",
            "길동이",
            "test@example.com",
            "password123!",
            null
        );

        // when & then
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isCreated())  // 201로 변경
            .andExpect(jsonPath("$.data.name").value("홍길동"))
            .andExpect(jsonPath("$.data.nickname").value("길동이"))
            .andExpect(jsonPath("$.data.email").value("test@example.com"))
            .andExpect(jsonPath("$.data.password").doesNotExist());

        // DB 검증
        @SuppressWarnings("unchecked")
        Optional<User> optionalUser = (Optional<User>) userRepository.findByEmail(
            "test@example.com");
        assertThat(optionalUser).isPresent();
        User savedUser = optionalUser.get();
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getName()).isEqualTo("홍길동");
        assertThat(savedUser.getNickname()).isEqualTo("길동이");
    }

    @Test
    @DisplayName("회원가입 - Validation 실패 (이름 누락)")
    void createUser_ValidationFail_NameBlank() throws Exception {
        // given
        UserCreateRequest request = new UserCreateRequest(
            "",
            "길동이",
            "test@example.com",
            "password123!",
            null
        );

        // when & then
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("회원가입 - Validation 실패 (이메일 형식 오류)")
    void createUser_ValidationFail_InvalidEmail() throws Exception {
        // given
        UserCreateRequest request = new UserCreateRequest(
            "홍길동",
            "길동이",
            "invalid-email",
            "password123!",
            null
        );

        // when & then
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("회원가입 - Validation 실패 (비밀번호 형식 오류)")
    void createUser_ValidationFail_InvalidPassword() throws Exception {
        // given
        UserCreateRequest request = new UserCreateRequest(
            "홍길동",
            "길동이",
            "test@example.com",
            "1234",
            null
        );

        // when & then
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("전체 회원 조회 통합 테스트")
    void getAllUsers_Integration() throws Exception {
        // given
        userRepository.save(UserFixture.createUserWithEmail("user1@example.com"));
        userRepository.save(UserFixture.createUserWithEmail("user2@example.com"));

        // when & then
        mockMvc.perform(get("/users"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(2))
            .andExpect(jsonPath("$.data[0].email").value("user1@example.com"))
            .andExpect(jsonPath("$.data[1].email").value("user2@example.com"));
    }

    @Test
    @DisplayName("특정 회원 조회 통합 테스트")
    void getUser_Integration() throws Exception {
        // given
        User user = userRepository.save(UserFixture.createDefaultUser());

        // when & then
        mockMvc.perform(get("/users/{id}", user.getId()))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(user.getId()))
            .andExpect(jsonPath("$.data.name").value("테스터"))
            .andExpect(jsonPath("$.data.nickname").value("테스트닉"))
            .andExpect(jsonPath("$.data.email").value("test@example.com"));
    }

    @Test
    @DisplayName("존재하지 않는 회원 조회 시 404 반환")
    void getUser_NotFound() throws Exception {
        // when & then
        mockMvc.perform(get("/users/{id}", 999L))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("회원 정보 수정 통합 테스트")
    void updateUser_Integration() throws Exception {
        // given
        User user = userRepository.save(UserFixture.createDefaultUser());
        Long userId = user.getId();
        String originalName = user.getName();
        String originalEmail = user.getEmail();

        UserUpdateRequest request = new UserUpdateRequest(
            "신이름",
            "신닉네임",
            "new@example.com",
            null,
            null,
            Role.USER
        );

        // when & then
        mockMvc.perform(put("/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name").value(originalName))
            .andExpect(jsonPath("$.data.nickname").value("신닉네임"))
            .andExpect(jsonPath("$.data.email").value(originalEmail));

        // DB 검증
        @SuppressWarnings("unchecked")
        Optional<User> optionalUser = (Optional<User>) userRepository.findById(userId);
        assertThat(optionalUser).isPresent();
        User updatedUser = optionalUser.get();
        assertThat(updatedUser.getName()).isEqualTo(originalName);
        assertThat(updatedUser.getNickname()).isEqualTo("신닉네임");
        assertThat(updatedUser.getEmail()).isEqualTo(originalEmail);
    }

    @Test
    @DisplayName("회원 정보 부분 수정 - nickname 만 수정")
    void updateUser_PartialUpdate() throws Exception {
        // given
        User user = userRepository.save(UserFixture.createDefaultUser());

        UserUpdateRequest request = new UserUpdateRequest(
            null,
            "신닉네임",
            null,
            null,
            null,
            Role.USER
        );

        // when & then
        mockMvc.perform(put("/users/{id}", user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name").value("테스터"))
            .andExpect(jsonPath("$.data.nickname").value("신닉네임"))
            .andExpect(jsonPath("$.data.email").value("test@example.com"));
    }

    @Test
    @DisplayName("회원 삭제 통합 테스트")
    void deleteUser_Integration() throws Exception {
        // given
        User user = userRepository.save(
            UserFixture.createUserWithEmail("delete@example.com")
        );

        // when & then
        mockMvc.perform(delete("/users/{id}", user.getId()))
            .andDo(print())
            .andExpect(status().isNoContent());

        // DB 검증
        assertThat(userRepository.findById(user.getId())).isEmpty();
    }

    @Test
    @DisplayName("중복 이메일로 회원가입 시 실패")
    void createUser_DuplicateEmail() throws Exception {
        // given
        userRepository.save(UserFixture.createUserWithEmail("duplicate@example.com"));

        UserCreateRequest request = new UserCreateRequest(
            "신규유저",
            "신규닉네임",
            "duplicate@example.com",
            "password123!",
            null
        );

        // when & then
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isConflict());  // 409로 변경
    }

    @Test
    @DisplayName("프로필 이미지와 함께 회원가입")
    void createUser_WithProfileImage() throws Exception {
        // given
        UserCreateRequest request = new UserCreateRequest(
            "홍길동",
            "길동이",
            "test@example.com",
            "password123!",
            "/images/profile.jpg"
        );

        // when & then
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isCreated())  // 201로 변경
            .andExpect(jsonPath("$.data.profileImage").value("/images/profile.jpg"));

        // DB 검증
        @SuppressWarnings("unchecked")
        Optional<User> optionalUser = (Optional<User>) userRepository.findByEmail(
            "test@example.com");
        assertThat(optionalUser).isPresent();
        User savedUser = optionalUser.get();
        assertThat(savedUser.getProfileImage()).isEqualTo("/images/profile.jpg");
    }
}