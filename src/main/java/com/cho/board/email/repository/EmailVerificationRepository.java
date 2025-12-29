package com.cho.board.email.repository;

import com.cho.board.email.entity.EmailVerification;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    // 아직 인증되지 않은 코드 찾기 (중복인증 방지)
    Optional<EmailVerification> findByEmailAndVerificationCodeAndVerifiedFalse(
        String email, String verificationCode);

    // 최신 인증 요청만 조회
    Optional<EmailVerification> findTopByEmailAndVerifiedFalseOrderByCreatedAtDesc(String email);

    // 인증기간 만료토큰 개수 세기
    long countByExpiryDateBefore(LocalDateTime dateTime);

    // 인증기간 만료토큰 삭제
    @Modifying
    @Transactional
    @Query("DELETE FROM EmailVerification e WHERE e.expiryDate < :dateTime")
    void deleteByExpiryDateBefore(@Param("dateTime") LocalDateTime dateTime);

    // 기존 미 인증 토큰 삭제
    @Modifying
    @Transactional
    void deleteByEmailAndVerifiedFalse(String email);
}
