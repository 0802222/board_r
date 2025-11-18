package com.cho.board.controller.user.dtos;

import com.cho.board.domain.user.Role;
import com.cho.board.domain.user.User;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserDetailResponse {

    private Long id;
    private String name;
    private String nickname;
    private String email;
    private String profileImage;
    private Role role;
    private LocalDateTime createdAt;

    public static UserDetailResponse from(User user) {
        return UserDetailResponse.builder()
            .id(user.getId())
            .name(user.getName())
            .nickname(user.getNickname())
            .email(user.getEmail())
            .profileImage(user.getProfileImage())
            .role(user.getRole())
            .createdAt(user.getCreatedAt())
            .build();
    }

}
