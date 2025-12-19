package com.cho.board.email.service;

import com.cho.board.email.entity.EmailVerification;
import com.cho.board.email.repository.EmailVerificationRepository;
import java.time.LocalDateTime;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailVerificationRepository verificationRepository;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendVerificationEmail(String toEmail) {

        // 1. 인증 코드 생성 (6자리 랜덤)
        String code = generateVerificationCode();

        // 2. 유효기간 설정 (10분)
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(10);

        // 3. DB 저장
        EmailVerification verification = EmailVerification.builder()
            .email(toEmail)
            .verificationCode(code)
            .expiryDate(expiryDate)
            .build();
        verificationRepository.save(verification);

        // 4. 이메일 전송
        sendEmail(toEmail, "이메일 인증 코드", "인증코드 : " + code + "\n유효시간: 10분");
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
