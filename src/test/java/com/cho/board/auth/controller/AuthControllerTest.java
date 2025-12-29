package com.cho.board.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cho.board.auth.dtos.LoginRequest;
import com.cho.board.auth.dtos.RefreshTokenRequest;
import com.cho.board.auth.dtos.SignupRequest;
import com.cho.board.auth.dtos.TokenResponse;
import com.cho.board.auth.service.AuthService;
import com.cho.board.config.TestSecurityConfig;
import com.cho.board.email.service.EmailService;
import com.cho.board.global.exception.CustomJwtException;
import com.cho.board.global.exception.DuplicateResourceException;
import com.cho.board.global.exception.ErrorCode;
import com.cho.board.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(TestSecurityConfig.class)
@DisplayName("AuthController 통합 테스트")
class AuthControllerTest {

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // 테스트에서 사용할 데이터들
    private SignupRequest signupRequest;
    private LoginRequest loginRequest;
    private RefreshTokenRequest refreshTokenRequest;
    private TokenResponse tokenResponse;

    @BeforeEach
    void setUp() {
        // SignupRequest 준비
        signupRequest = SignupRequest.builder()
            .email("test@test.com")
            .password("test1234@")
            .name("테스트")
            .nickname("테스터")
            .build();

        // LoginRequest 준비
        loginRequest = LoginRequest.builder()
            .email("test@test.com")
            .password("test1234@")
            .build();

        // RefreshTokenRequest 준비
        refreshTokenRequest = new RefreshTokenRequest();

        // TokenResponse 준비
        tokenResponse = new TokenResponse(
            "eyJhbGciOiJIUzI1NiJ9.accessToken",
            "eyJhbGciOiJIUzI1NiJ9.refreshToken"
        );
    }

    // ========== 회원가입 테스트 ==========

    @Test
    @DisplayName("회원가입 성공")
    void signup_Success() throws Exception {
        // given
        willDoNothing().given(authService).signup(any(SignupRequest.class));

        // when & then
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("회원가입에 성공했습니다."));
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 형식 오류")
    void signup_Fail_InvalidEmail() throws Exception {
        // given
        SignupRequest invalidRequest = SignupRequest.builder()
            .email("invalid-email")  // 잘못된 이메일 형식
            .password("test1234@")
            .name("테스트")
            .nickname("테스터")
            .build();

        // when & then
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("회원가입 실패 - 비밀번호 길이 부족")
    void signup_Fail_ShortPassword() throws Exception {
        // given
        SignupRequest invalidRequest = SignupRequest.builder()
            .email("test@test.com")
            .password("short")  // 8자 미만
            .name("테스트")
            .nickname("테스터")
            .build();

        // when & then
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("회원가입 실패 - 중복 이메일")
    void signup_Fail_DuplicateEmail() throws Exception {
        // given
        willThrow(new DuplicateResourceException(ErrorCode.EMAIL_ALREADY_EXISTS, "이미 존재하는 이메일입니다."))
            .given(authService).signup(any(SignupRequest.class));

        // when & then
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
            .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("회원가입 실패 - 필수 필드 누락")
    void signup_Fail_MissingFields() throws Exception {
        // given
        SignupRequest invalidRequest = SignupRequest.builder()
            .email("test@test.com")
            // password, name, nickname 누락
            .build();

        // when & then
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    // ========== 로그인 테스트 ==========

    @Test
    @DisplayName("로그인 성공")
    void login_Success() throws Exception {
        // given
        given(authService.login(any(LoginRequest.class)))
            .willReturn(tokenResponse);

        // when & then
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("로그인에 성공했습니다."))
            .andExpect(jsonPath("$.data.accessToken").value(tokenResponse.getAccessToken()))
            .andExpect(jsonPath("$.data.refreshToken").value(tokenResponse.getRefreshToken()))
            .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 비밀번호")
    void login_Fail_WrongPassword() throws Exception {
        // given
        willThrow(new BadCredentialsException("자격 증명에 실패하였습니다."))
            .given(authService).login(any(LoginRequest.class));

        // when & then
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("로그인 실패 - 이메일 형식 오류")
    void login_Fail_InvalidEmail() throws Exception {
        // given
        LoginRequest invalidRequest = LoginRequest.builder()
            .email("invalid-email")
            .password("test1234@")
            .build();

        // when & then
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("로그인 실패 - 필수 필드 누락")
    void login_Fail_MissingFields() throws Exception {
        // given
        LoginRequest invalidRequest = LoginRequest.builder()
            .email("test@test.com")
            // password 누락
            .build();

        // when & then
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    // ========== 토큰 갱신 테스트 ==========

    @Test
    @DisplayName("토큰 갱신 성공")
    void refreshToken_Success() throws Exception {
        // given
        TokenResponse newTokenResponse = new TokenResponse(
            "eyJhbGciOiJIUzI1NiJ9.newAccessToken",
            "eyJhbGciOiJIUzI1NiJ9.refreshToken"
        );

        given(authService.refreshToken(anyString()))
            .willReturn(newTokenResponse);

        // when & then
        mockMvc.perform(post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"eyJhbGciOiJIUzI1NiJ9.refreshToken\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.accessToken").value(newTokenResponse.getAccessToken()))
            .andExpect(jsonPath("$.data.refreshToken").value(newTokenResponse.getRefreshToken()));
    }

    @Test
    @DisplayName("토큰 갱신 실패 - 유효하지 않은 토큰")
    void refreshToken_Fail_InvalidToken() throws Exception {
        // given
        willThrow(new CustomJwtException("유효하지 않은 Refresh Token 입니다."))
            .given(authService).refreshToken(anyString());

        // when & then
        mockMvc.perform(post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"invalid-token\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("토큰 갱신 실패 - 토큰 누락")
    void refreshToken_Fail_MissingToken() throws Exception {
        // given
        RefreshTokenRequest emptyRequest = new RefreshTokenRequest();

        // when & then
        mockMvc.perform(post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyRequest)))
            .andExpect(status().isBadRequest());
    }

    // ========== 이메일 인증 테스트 ==========

    @Test
    @DisplayName("이메일 인증 코드 전송 성공")
    void sendVerificationEmail_Success() throws Exception {
        // given
        given(userRepository.existsByEmail(anyString())).willReturn(false);
        willDoNothing().given(emailService)
            .validateAndPrepareVerification(anyString());

        // when & then
        mockMvc.perform(post("/auth/email/send-verification")
                .param("email", "test@test.com"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("인증코드가 전송되었습니다."));
    }

    @Test
    @DisplayName("이메일 인증 코드 전송 실패 - 이미 가입된 이메일")
    void sendVerificationEmail_Fail_AlreadyExists() throws Exception {
        // given
        given(userRepository.existsByEmail("test@test.com")).willReturn(true);

        // when & then
        mockMvc.perform(post("/auth/email/send-verification")
                .param("email", "test@test.com"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("이미 가입된 이메일입니다."));
    }

    @Test
    @DisplayName("이메일 인증 코드 전송 실패 - 5분 제한")
    void sendVerificationEmail_Fail_RateLimit() throws Exception {
        // given
        given(userRepository.existsByEmail(anyString())).willReturn(false);
        willThrow(new IllegalStateException("인증코드를 295초 후에 재발송할 수 있습니다."))
            .given(emailService).validateAndPrepareVerification(anyString());

        // when & then
        mockMvc.perform(post("/auth/email/send-verification")
                .param("email", "test@test.com"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("인증코드를 295초 후에 재발송할 수 있습니다."));
    }

    @Test
    @DisplayName("이메일 인증 확인 성공")
    void verifyEmail_Success() throws Exception {
        // given
        willDoNothing().given(emailService).verifyEmail(anyString(), anyString());

        // when & then
        mockMvc.perform(post("/auth/email/verify")
                .param("email", "test@test.com")
                .param("code", "123456"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("이메일 인증이 완료되었습니다."));
    }

    @Test
    @DisplayName("이메일 인증 확인 실패 - 잘못된 코드")
    void verifyEmail_Fail_InvalidCode() throws Exception {
        // given
        willThrow(new IllegalArgumentException("잘못된 인증 코드입니다."))
            .given(emailService).verifyEmail(anyString(), anyString());

        // when & then
        mockMvc.perform(post("/auth/email/verify")
                .param("email", "test@test.com")
                .param("code", "wrong-code"))
            .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("이메일 인증 확인 실패 - 만료된 코드")
    void verifyEmail_Fail_ExpiredCode() throws Exception {
        // given
        willThrow(new IllegalArgumentException("인증 코드가 만료되었습니다."))
            .given(emailService).verifyEmail(anyString(), anyString());

        // when & then
        mockMvc.perform(post("/auth/email/verify")
                .param("email", "test@test.com")
                .param("code", "123456"))
            .andExpect(status().isInternalServerError());
    }
}