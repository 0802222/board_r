package com.cho.board.auth.service;

import com.cho.board.auth.dtos.LoginRequest;
import com.cho.board.auth.dtos.LoginResponse;
import com.cho.board.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;

    public LoginResponse login(LoginRequest request) {
        // 1. AuthenticationManager 에게 인증 요청
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
            )
        );

        // 2. 인증 성공하면 Authentication 객체에 UserDetails 가 담겨있음
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // 3. 응답 생성
        return LoginResponse.of(
            userDetails.getUserId(),
            userDetails.getUsername(),
            userDetails.getUser().getName(),
            userDetails.getUser().getRole().name()
        );
    }
}
