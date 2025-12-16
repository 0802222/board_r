package com.cho.board.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cho.board.auth.dtos.LoginRequest;
import com.cho.board.auth.dtos.SignupRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("security-test")  // JWT 인증 테스트를 위해 실제 SecurityConfig 사용
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String testEmail = "security@test.com";
    private String testPassword = "password123!";
    private String testName = "TestUser";
    private String testNickname = "testNick";

    @BeforeEach
    void setUp() throws Exception {
        // 테스트용 사용자 회원가입
        SignupRequest signupRequest = SignupRequest.builder()
            .email(testEmail)
            .password(testPassword)
            .name(testName)
            .nickname(testNickname)
            .build();

        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
            .andExpect(status().isOk());
    }

    @Test
    void JWT_없이_보호된_API_접근시_401() throws Exception {
        mockMvc.perform(get("/posts"))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }

    @Test
    void 유효한_JWT로_보호된_API_접근_성공() throws Exception {
        // 1. 로그인해서 토큰 받기
        String token = 로그인_후_토큰_추출();

        // 2. 토큰과 함께 요청
        mockMvc.perform(get("/posts")
                .header("Authorization", "Bearer " + token))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    void 잘못된_JWT로_접근시_401() throws Exception {
        String invalidToken = "invalid.jwt.token";

        mockMvc.perform(get("/posts")
                .header("Authorization", "Bearer " + invalidToken))
            .andDo(print())
            .andExpect(status().isUnauthorized());  // 401
    }

    @Test
    void Bearer_없이_토큰만_보내면_401() throws Exception {
        String token = 로그인_후_토큰_추출();

        mockMvc.perform(get("/posts")
                .header("Authorization", token))  // Bearer 없음
            .andDo(print())
            .andExpect(status().isUnauthorized());  // 401
    }

    @Test
    void 로그인_성공시_AccessToken과_RefreshToken_반환() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
            .email(testEmail)
            .password(testPassword)
            .build();

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.accessToken").exists())
            .andExpect(jsonPath("$.data.refreshToken").exists())
            .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }

    // 헬퍼 메서드: 로그인 후 토큰 추출
    private String 로그인_후_토큰_추출() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
            .email(testEmail)
            .password(testPassword)
            .build();

        MvcResult result = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn();

        String response = result.getResponse().getContentAsString();

        // JSON 에서 accessToken 추출
        return objectMapper.readTree(response)
            .get("data")
            .get("accessToken")
            .asText();
    }
}
