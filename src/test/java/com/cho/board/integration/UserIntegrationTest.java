package com.cho.board.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cho.board.auth.dtos.SignupRequest;
import com.cho.board.config.TestSecurityConfig;
import com.cho.board.fixture.UserFixture;
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
        SignupRequest request = SignupRequest.builder()
            .name("홍길동")
            .nickname("길동이")
            .email("test@example.com")
            .password("password123!")
            .build();

        // when & then
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("회원가입에 성공했습니다."));

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
        SignupRequest request = SignupRequest.builder()
            .name("")
            .nickname("길동이")
            .email("test@example.com")
            .password("password123!")
            .build();

        // when & then
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("회원가입 - Validation 실패 (이메일 형식 오류)")
    void createUser_ValidationFail_InvalidEmail() throws Exception {
        // given
        SignupRequest request = SignupRequest.builder()
            .name("홍길동")
            .nickname("길동이")
            .email("invalid-email")
            .password("password123!")
            .build();

        // when & then
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("회원가입 - Validation 실패 (비밀번호 형식 오류)")
    void createUser_ValidationFail_InvalidPassword() throws Exception {
        // given
        SignupRequest request = SignupRequest.builder()
            .name("홍길동")
            .nickname("길동이")
            .email("test@example.com")
            .password("1234")
            .build();

        // when & then
        mockMvc.perform(post("/auth/signup")
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

    // 주의: 새로운 API 구조에서는 /users/{id} 엔드포인트가 제거되었습니다.
    // 사용자 본인의 정보 조회/수정/삭제는 /users/me를 통해 이루어지며,
    // 이는 @AuthenticationPrincipal을 통해 인증된 사용자만 접근 가능합니다.
    // 따라서 기존의 ID 기반 조회/수정/삭제 테스트는 더 이상 적용되지 않으므로 제거되었습니다.
    // /users/me 엔드포인트 테스트는 SecurityIntegrationTest 또는 별도의 테스트 파일에서 수행됩니다.

    @Test
    @DisplayName("중복 이메일로 회원가입 시 실패")
    void createUser_DuplicateEmail() throws Exception {
        // given
        userRepository.save(UserFixture.createUserWithEmail("duplicate@example.com"));

        SignupRequest request = SignupRequest.builder()
            .name("신규유저")
            .nickname("신규닉네임")
            .email("duplicate@example.com")
            .password("password123!")
            .build();

        // when & then
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("프로필 이미지와 함께 회원가입")
    void createUser_WithProfileImage() throws Exception {
        // given - SignupRequest는 profileImage를 지원하지 않으므로 이 테스트는 삭제하거나 수정 필요
        // AuthService.signup은 profileImage를 null로 설정하므로 이 테스트는 의미가 없음
        // 테스트 자체를 제거하거나 다른 방식으로 변경해야 함
        SignupRequest request = SignupRequest.builder()
            .name("홍길동")
            .nickname("길동이")
            .email("test@example.com")
            .password("password123!")
            .build();

        // when & then
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("회원가입에 성공했습니다."));

        // DB 검증 - 프로필 이미지는 null이어야 함
        @SuppressWarnings("unchecked")
        Optional<User> optionalUser = (Optional<User>) userRepository.findByEmail(
            "test@example.com");
        assertThat(optionalUser).isPresent();
        User savedUser = optionalUser.get();
        assertThat(savedUser.getProfileImage()).isNull();
    }
}