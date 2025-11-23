package com.cho.board.user.dtos;

import com.cho.board.user.entity.Role;
import com.cho.board.user.entity.User;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserListResponse {

    private long id;
    private String name;
    private String nickname;
    private String email;
    private String profileImage;
    private Role role;
    private LocalDateTime createdAt;

    public static UserListResponse from(User user) {
        return UserListResponse.builder()
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
