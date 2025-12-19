package com.cho.board.auth.controller;

import com.cho.board.auth.dtos.LoginRequest;
import com.cho.board.auth.dtos.RefreshTokenRequest;
import com.cho.board.auth.dtos.SignupRequest;
import com.cho.board.auth.dtos.TokenResponse;
import com.cho.board.auth.service.AuthService;
import com.cho.board.email.service.EmailService;
import com.cho.board.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final EmailService emailService;

    @PostMapping("/email/send-verification")
    public ResponseEntity<ApiResponse<Void>> sendVerification(@RequestParam String email) {
        emailService.sendVerificationEmail(email);

        return ResponseEntity.ok(ApiResponse.success(null, "인증코드가 전송되었습니다."));
    }

    @PostMapping("/email/verify")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
        @RequestParam String email,
        @RequestParam String code) {
        emailService.verifyEmail(email, code);

        return ResponseEntity.ok(ApiResponse.success(null, "이메일 인증이 완료되었습니다."));
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@Valid @RequestBody SignupRequest request) {
        authService.signup(request);

        return ResponseEntity.ok(ApiResponse.success("회원가입에 성공했습니다."));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(
        @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(
            ApiResponse.success(authService.login(request), "로그인에 성공했습니다.")
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(
        @Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(
            ApiResponse.success(authService.refreshToken(request.getRefreshToken())));
    }
}
