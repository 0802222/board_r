package com.cho.board.user.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.assertj.core.api.Assertions.*;


import com.cho.board.global.exception.BusinessException;
import com.cho.board.global.exception.DuplicateResourceException;
import com.cho.board.global.exception.ErrorCode;
import com.cho.board.global.exception.ResourceNotFoundException;
import com.cho.board.user.dtos.UserCreateRequest;
import com.cho.board.user.dtos.UserUpdateRequest;
import com.cho.board.user.entity.Role;
import com.cho.board.user.entity.User;
import com.cho.board.user.repository.UserRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 단위 테스트")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks // 테스트 대상(Service) 위에 Mock 객체 (Repository) 들이 주입됨
    private UserService userService;

    // 테스트에서 사용할 데이터들
    private User user;
    private UserCreateRequest createRequest;
    private UserUpdateRequest updateRequest;

    // 각 테스트 전에 실행
    @BeforeEach
    void setUp() {
        // 모든 테스트에서 사용할 테스트 데이터 준비
        user = User.builder()
            .name("홍길동")
            .nickname("테스트유저")
            .email("test@example.com")
            .password("encodedPassword")
            .profileImage("profile.jpg")
            .role(Role.USER)
            .build();

        // ID 는 reflection 으로 설정 (빌더에 없을 수 있으므로)
        ReflectionTestUtils.setField(user, "id", 1L);

        createRequest = new UserCreateRequest();
        ReflectionTestUtils.setField(createRequest, "name", "새유저");
        ReflectionTestUtils.setField(createRequest, "nickname", "새닉네임");
        ReflectionTestUtils.setField(createRequest, "email", "new@example.com");
        ReflectionTestUtils.setField(createRequest, "password", "Password123!");
        ReflectionTestUtils.setField(createRequest, "profileImage", "new-profile.jpg");

        updateRequest = new UserUpdateRequest();
        ReflectionTestUtils.setField(updateRequest, "nickname", "수정된닉네임");
        ReflectionTestUtils.setField(updateRequest, "profileImage", "updated-profile.jpg");
        ReflectionTestUtils.setField(updateRequest, "password", "NewPassword123!");

        System.out.println("setUp 실행 됨! 테스트 데이터 준비 완료");

    }

    @Test
    @DisplayName("회원가입 성공")
    void create() {
        // given
        given(userRepository.findByName(anyString())).willReturn(Optional.empty());
        given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());
        given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
        given(userRepository.save(any(User.class))).willReturn(user);

        // when
        User result = userService.create(createRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("홍길동");
        assertThat(result.getEmail()).isEqualTo("test@example.com");

        then(userRepository).should().findByName("새닉네임");
        then(userRepository).should().findByEmail("new@example.com");
        then(passwordEncoder).should().encode("Password123!");
        then(userRepository).should().save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 중복된 username")
    void create_Fail_DuplicatedUsername() {
        // given
        given(userRepository.findByName(anyString())).willReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> userService.create(createRequest))
            .isInstanceOf(DuplicateResourceException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NICKNAME_ALREADY_EXISTS);

        then(userRepository).should(never()).save(any(User.class));
    }

    @Test
    @DisplayName("전체 사용자 조회")
    void findAll() {
        // given
        List<User> users = Arrays.asList(user);
        given(userRepository.findAll()).willReturn(users);

        // when
        List<User> result = userService.findAll();

        // then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getName()).isEqualTo("홍길동");
        assertThat(result.get(0).getEmail()).isEqualTo("test@example.com");

        then(userRepository).should().findAll();
    }

    @Test
    @DisplayName("ID로 사용자 조회 성공")
    void findById_Success() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        // when
        User result = userService.findById(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("홍길동");
        assertThat(result.getEmail()).isEqualTo("test@example.com");

        then(userRepository).should().findById(1L);
    }

    @Test
    @DisplayName("ID로 사용자 조회 실패 - 존재하지 않음")
    void findById_Fail_NotFound() {
        // given
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.findById(999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND)
            .hasMessageContaining("사용자를 찾을 수 없습니다. ID: 999");

        then(userRepository).should().findById(999L);
    }

    @Test
    @DisplayName("사용자 수정 성공 - 닉네임과 프로필 이미지만")
    void update_Success_NicknameAndProfileImage() {
        // given
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        ReflectionTestUtils.setField(updateRequest, "nickname", "수정된닉네임");
        ReflectionTestUtils.setField(updateRequest, "profileImage", "updated.jpg");

        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        // when
        User result = userService.update(1L, updateRequest);

        // then
        assertThat(result).isNotNull();

        then(userRepository).should().findById(1L);
    }

    @Test
    @DisplayName("사용자 수정 성공 - 비밀번호 포함")
    void update_Success_WithPassword() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);
        given(passwordEncoder.encode(anyString())).willReturn("newEncodedPassword");

        // when
        User result = userService.update(1L, updateRequest);

        // then
        assertThat(result).isNotNull();
        then(userRepository).should().findById(1L);
        then(passwordEncoder).should().matches("NewPassword123!", "encodedPassword");
        then(passwordEncoder).should().encode("NewPassword123!");
    }

    @Test
    @DisplayName("사용자 수정 실패 - 같은 비밀번호")
    void update_Fail_SamePassword() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.update(1L, updateRequest))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SAME_PASSWORD);

        then(passwordEncoder).should(never()).encode(anyString());
    }

    @Test
    @DisplayName("사용자 수정 실패 - 존재하지 않음")
    void update_Fail_NotFound() {
        // given
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.update(999L, updateRequest))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

        then(userRepository).should().findById(999L);
    }

    @Test
    @DisplayName("사용자 삭제 성공")
    void delete_Success() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        // when
        userService.delete(1L);

        // then
        then(userRepository).should().findById(1L);
        then(userRepository).should().delete(user);
    }

    @Test
    @DisplayName("사용자 삭제 실패 - 존재하지 않음")
    void delete_Fail_NotFound() {
        // given
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.delete(999L))
            .isInstanceOf(ResourceNotFoundException.class);

        then(userRepository).should().findById(999L);
        then(userRepository).should(never()).delete(any(User.class));
    }
}