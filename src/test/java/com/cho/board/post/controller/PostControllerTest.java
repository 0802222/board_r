package com.cho.board.post.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cho.board.config.TestSecurityConfig;
import com.cho.board.post.dtos.PostCreateRequest;
import com.cho.board.post.dtos.PostDetailResponse;
import com.cho.board.post.dtos.PostUpdateRequest;
import com.cho.board.post.service.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(TestSecurityConfig.class)
@DisplayName("PostController 통합 테스트")
class PostControllerTest {

    @MockitoBean
    private PostService postService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // 테스트에서 사용할 데이터들
    private PostCreateRequest createRequest;
    private PostUpdateRequest updateRequest;
    private PostDetailResponse postResponse;

    @BeforeEach
    void setUp() {
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
            .categoryId(1L)
            .build();

        // Response 준비
        postResponse = PostDetailResponse.builder()
            .id(1L)
            .title("제목")
            .content("내용")
            .categoryId(1L)
            .build();
    }

    @Test
    @WithMockUser(username = "test@test.com", roles = "USER")
    @DisplayName("게시글 생성 성공")
    void create_Success() throws Exception {
        // given
        given(postService.create(eq("test@test.com"), any(PostCreateRequest.class)))
            .willReturn(postResponse);

        // when & then
        mockMvc.perform(post("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())  // 201 Created
            .andExpect(jsonPath("$.data.id").value(1L))
            .andExpect(jsonPath("$.data.title").value("제목"))
            .andExpect(jsonPath("$.data.content").value("내용"))
            .andExpect(jsonPath("$.data.categoryId").value(1L));
    }

    @Test
    @WithMockUser(username = "test@test.com", roles = "USER")
    @DisplayName("게시글 수정 성공")
    void update_Success() throws Exception {
        // given
        PostDetailResponse updatedResponse = PostDetailResponse.builder()
            .id(1L)
            .title("수정된 제목")
            .content("수정된 내용")
            .categoryId(1L)
            .build();

        given(postService.update(eq(1L), any(PostUpdateRequest.class), eq("test@test.com")))
            .willReturn(updatedResponse);

        // when & then
        mockMvc.perform(put("/posts/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())  // 200 OK
            .andExpect(jsonPath("$.data.id").value(1L))
            .andExpect(jsonPath("$.data.title").value("수정된 제목"))
            .andExpect(jsonPath("$.data.content").value("수정된 내용"))
            .andExpect(jsonPath("$.data.categoryId").value(1L));
    }

    @Test
    @WithMockUser(username = "test@test.com", roles = "USER")
    @DisplayName("게시글 삭제 성공")
    void delete_Success() throws Exception {
        // given
        willDoNothing().given(postService).delete(eq(1L), eq("test@test.com"));

        // when & then
        mockMvc.perform(delete("/posts/{id}", 1L))
            .andExpect(status().isNoContent());  // 204 No Content
    }
}