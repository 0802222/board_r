package com.cho.board.email.service;

import com.cho.board.email.entity.EmailVerification;
import com.cho.board.email.repository.EmailVerificationRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailVerificationRepository verificationRepository;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // 1. 동기 검증 (예외 발생 가능)
    public void validateAndPrepareVerification(String toEmail) {

        // 0. 최근 인증 코드 발송 내역 확인
        Optional<EmailVerification> recent = verificationRepository
            .findTopByEmailAndVerifiedFalseOrderByCreatedAtDesc(toEmail);

        if (recent.isPresent()) {
            LocalDateTime lastSent = recent.get().getCreatedAt();
            LocalDateTime now = LocalDateTime.now();

            if (lastSent.plusMinutes(5).isAfter(now)) {
                long remainSeconds = Duration.between(now, lastSent.plusMinutes(5)).getSeconds();
                throw new IllegalStateException(
                    "인증코드를 " + remainSeconds + "초 후에 재발송 할 수 있습니다."
                );
            }
        }

        // 1. 기존 미 인증 토큰 삭제
        verificationRepository.deleteByEmailAndVerifiedFalse(toEmail);

        // 2. 인증 코드 생성 (6자리 랜덤)
        String code = generateVerificationCode();

        // 3. 유효기간 설정 (10분)
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(10);

        // 4. DB 저장
        EmailVerification verification = EmailVerification.builder()
            .email(toEmail)
            .verificationCode(code)
            .expiryDate(expiryDate)
            .build();
        verificationRepository.save(verification);

        // 비동기 발송 호출
        sendVerificationEmailAsync(toEmail, code);
    }

    // 2. 비동기 이메일 전송
    @Async
    public void sendVerificationEmailAsync(String toEmail, String code) {
        try {
            sendEmail(toEmail, "이메일 인증 코드", "인증 코드: " + code + "\n유효시간: 10분");
        } catch (Exception e) {
            log.error("이메일 전송 실패: {}, 코드: {}", toEmail, code, e);
            // 실패해도 DB 에는 저장되어 있음 -> 재발송 가능
        }
    }


    private String generateVerificationCode() {
        return String.valueOf(100000 + new Random().nextInt(900000));
    }

    private void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }

    public void verifyEmail(String email, String code) {
        EmailVerification verification = verificationRepository
            .findByEmailAndVerificationCodeAndVerifiedFalse(email, code)
            .orElseThrow(() -> new IllegalArgumentException("잘못된 인증 코드입니다."));

        if (verification.isExpired()) {
            throw new IllegalArgumentException("인증 코드가 만료되었습니다.");
        }

        verification.verify();
    }
}
