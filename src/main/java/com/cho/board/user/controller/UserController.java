package com.cho.board.user.controller;

import com.cho.board.global.response.ApiResponse;
import com.cho.board.user.dtos.UserCreateRequest;
import com.cho.board.user.dtos.UserDetailResponse;
import com.cho.board.user.dtos.UserListResponse;
import com.cho.board.user.dtos.UserUpdateRequest;
import com.cho.board.user.entity.User;
import com.cho.board.user.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<UserDetailResponse>> create(
        @RequestBody @Valid UserCreateRequest request) {
        User user = userService.create(request);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(UserDetailResponse.from(user),
                "회원가입이 완료되었습니다."));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserListResponse>>> findAll() {
        List<UserListResponse> users = userService.findAll()
            .stream()
            .map(UserListResponse::from)
            .toList();

        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping(("/{userId}"))
    public ResponseEntity<ApiResponse<UserDetailResponse>> find(@PathVariable Long userId) {
        User user = userService.findById(userId);

        return ResponseEntity.ok(ApiResponse.success(UserDetailResponse.from(user)));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserDetailResponse>> update(
        @PathVariable Long userId,
        @Valid @RequestBody UserUpdateRequest request) {

        User user = userService.update(userId, request);

        return ResponseEntity.ok(ApiResponse.success(UserDetailResponse.from(user),
            "회원 정보가 수정되었습니다."));
    }

    @PostMapping("/{userId}/profile-image")
    public ResponseEntity<ApiResponse<UserDetailResponse>> uploadProfileImage(
        @PathVariable Long userId,
        @RequestParam("file") MultipartFile file) {

        User user = userService.updateProfileImage(userId, file);
        return ResponseEntity.ok(ApiResponse.success(UserDetailResponse.from(user),
            "프로필 이미지가 업로드 되었습니다."));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long userId) {
        userService.delete(userId);

        return ResponseEntity.noContent().build();
    }
}
