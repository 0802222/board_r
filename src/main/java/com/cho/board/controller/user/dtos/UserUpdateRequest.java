package com.cho.board.controller.user.dtos;

import com.cho.board.domain.user.Role;
import lombok.Getter;

@Getter
public class UserUpdateRequest {

    private String name;

    private String nickname;

    private String email;

    private String password;

    private String profileImage;

    private Role role = Role.USER;

}
