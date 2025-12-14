package com.cho.board.auth.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class LoginResponse {

    private Long userId;
    private String email;
    private String name;
    private String role;
    private String message;

    public static LoginResponse of(Long userId, String email, String name, String role) {
        return LoginResponse.builder()
            .userId(userId)
            .email(email)
            .name(name)
            .role(role)
            .message("로그인에 성공했습니다.")
            .build();
    }
}
