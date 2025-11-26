package com.cho.board.fixture;

import com.cho.board.user.entity.Role;
import com.cho.board.user.entity.User;

public class UserFixture {

    public static User createDefaultUser() {
        return User.builder()
            .name("테스터")
            .nickname("테스트닉")
            .email("test@example.com")
            .password("password123!")
            .role(Role.USER)
            .build();
    }

    // 커스텀 정보로 유저 생성
    public static User createUser(String name, String nickname, String email, String password) {
        return User.builder()
            .name(name)
            .nickname(nickname)
            .email(email)
            .password(password)
            .role(Role.USER)
            .build();
    }

    // 커스텀 이메일로 유저 생성 (테스트 격리 시 유용)
    public static User createUserWithEmail(String email) {
        return User.builder()
            .name("테스터")
            .nickname("테스트닉")
            .email(email)
            .password("password123!")
            .role(Role.USER)
            .build();
    }

    // 관리자 유저 생성
    public static User createAdmin() {
        return User.builder()
            .name("관리자")
            .nickname("어드민")
            .email("admin@example.com")
            .password("admin123!")
            .role(Role.ADMIN)
            .build();
    }

    // 커스텀 이메일로 관리자 생성
    public static User createAdminWithEmail(String email) {
        return User.builder()
            .name("관리자")
            .nickname("어드민")
            .email(email)
            .password("admin123!")
            .role(Role.ADMIN)
            .build();
    }

    // 프로필 이미지가 있는 유저 생성
    public static User createUserWithProfileImage(String profileImageUrl) {
        return User.builder()
            .name("이미지유저")
            .nickname("이미지닉")
            .email("image@example.com")
            .password("password123!")
            .profileImage(profileImageUrl)
            .role(Role.USER)
            .build();
    }

    // 커스텀 이메일과 프로필 이미지로 유저 생성
    public static User createUserWithEmailAndImage(String email, String profileImageUrl) {
        return User.builder()
            .name("이미지유저")
            .nickname("이미지닉")
            .email(email)
            .password("password123!")
            .profileImage(profileImageUrl)
            .role(Role.USER)
            .build();
    }

    // 여러 유저를 한번에 생성 (페이징 테스트 등에 유용)
    public static java.util.List<User> createMultipleUsers(int count) {
        return java.util.stream.IntStream.range(0, count)
            .mapToObj(i -> User.builder()
                .name("유저" + (i + 1))
                .nickname("닉네임" + (i + 1))
                .email("user" + (i + 1) + "@example.com")
                .password("password" + (i + 1))
                .role(Role.USER)
                .build())
            .collect(java.util.stream.Collectors.toList());
    }
}