package com.cho.board.user.controller;

import com.cho.board.global.response.ApiResponse;
import com.cho.board.user.dtos.PasswordChangeRequest;
import com.cho.board.user.dtos.UserDetailResponse;
import com.cho.board.user.dtos.UserListResponse;
import com.cho.board.user.dtos.UserUpdateRequest;
import com.cho.board.user.entity.User;
import com.cho.board.user.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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


    // ========== 관리자 전용 API ==========

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserListResponse>>> getAllUsers() {
        List<UserListResponse> users = userService.findAll()
            .stream()
            .map(UserListResponse::from)
            .toList();

        return ResponseEntity.ok(ApiResponse.success(users, "전체 사용자 목록 조회 성공"));
    }

    // ========== 인증된 사용자 본인 API (/me) ==========

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserDetailResponse>> getMyProfile(
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();
        User user = userService.findByEmail(email);

        return ResponseEntity.ok(ApiResponse.success(UserDetailResponse.from(user)));
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserDetailResponse>> updateMyProfile(
        @AuthenticationPrincipal UserDetails userDetails,
        @Valid @RequestBody UserUpdateRequest request
    ) {
        String email = userDetails.getUsername();
        User user = userService.updateByEmail(email, request);

        return ResponseEntity.ok(ApiResponse.success(UserDetailResponse.from(user),
            "프로필 정보가 수정되었습니다."));
    }

    @PutMapping("/me/profile-image")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserDetailResponse>> updateProfileImage(
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestParam("file") MultipartFile file
    ) {
        String email = userDetails.getUsername();
        User user = userService.updateProfileImageByEmail(email, file);

        return ResponseEntity.ok(ApiResponse.success(UserDetailResponse.from(user),
            "프로필 이미지가 변경되었습니다."));
    }

    @PutMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> changePassword(
        @AuthenticationPrincipal UserDetails userDetails,
        @Valid @RequestBody PasswordChangeRequest request
    ) {
        String email = userDetails.getUsername();
        userService.changePassword(email, request.getCurrentPassword(), request.getNewPassword());

        return ResponseEntity.ok(ApiResponse.success(null, "비밀번호가 변경되었습니다."));
    }

    @DeleteMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteMyAccount(
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();
        userService.deleteByEmail(email);

        return ResponseEntity.noContent().build();
    }
}
