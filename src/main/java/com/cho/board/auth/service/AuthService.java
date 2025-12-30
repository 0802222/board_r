package com.cho.board.auth.service;

import com.cho.board.auth.dtos.LoginRequest;
import com.cho.board.auth.dtos.SignupRequest;
import com.cho.board.auth.dtos.TokenResponse;
import com.cho.board.global.exception.CustomJwtException;
import com.cho.board.global.exception.DuplicateResourceException;
import com.cho.board.global.exception.ErrorCode;
import com.cho.board.global.util.JwtUtil;
import com.cho.board.user.entity.User;
import com.cho.board.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public void signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(ErrorCode.EMAIL_ALREADY_EXISTS, "이미 존재하는 이메일입니다.");
        }

        User user = User.builder()
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .name(request.getName())
            .nickname(request.getNickname())
            .build();

        userRepository.save(user);
    }

    public TokenResponse login(LoginRequest request) {
        // 1. AuthenticationManager 에게 인증 요청
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
            )
        );

        // 2. 인증 성공 시 토큰 생성
        String email = authentication.getName();
        String accessToken = jwtUtil.generateAccessToken(email);
        String refreshToken = jwtUtil.generateRefreshToken(email);

        // 3. 응답 생성
        return new TokenResponse(accessToken, refreshToken);
    }

    public TokenResponse refreshToken(String refreshToken) {

        // 1. RefreshToken 검증
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new CustomJwtException("유효하지 않은 Refresh Token 입니다.");
        }

        // 2. RefreshToken 에서 이메일 추출
        String email = jwtUtil.getEmailFromToken(refreshToken);

        // 3. 새로운 AccessToken 발급 (RefreshToken 은 재사용)
        String newAccessToken = jwtUtil.generateAccessToken(email);

        return new TokenResponse(newAccessToken, refreshToken);
    }
}
