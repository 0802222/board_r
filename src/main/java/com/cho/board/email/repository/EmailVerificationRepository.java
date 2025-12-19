package com.cho.board.email.repository;

import com.cho.board.email.entity.EmailVerification;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    // 아직 인증되지 않은 코드 찾기 (중복인증 방지)
    Optional<EmailVerification> findByEmailAndVerificationCodeAndVerifiedFalse(
        String email, String verificationCode);

    // 최신 인증 요청만 조회 (재전송 시  이전코드 무시)
    Optional<EmailVerification> findTopByEmailOrderByCreatedAtDesc(String email);

}
