package com.cho.board.email.service;

import com.cho.board.email.repository.EmailVerificationRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledTasks {

    private final EmailVerificationRepository tokenRepository;

    @Transactional
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();

        long expiredCount = tokenRepository.countByExpiryDateBefore(now);

        if (expiredCount > 0) {
            tokenRepository.deleteByExpiryDateBefore(now);
            log.info("만료된 이메일 인증 토큰 {}개 삭제 완료: {}", expiredCount, now);
        } else {
            log.debug("삭제할 만료 토큰 없음: {}", now);
        }
    }

//    @Scheduled(fixedRate = 10000)
//    public void healthCheck() {
//        log.info("스케줄러 동작 확인: {}", LocalDateTime.now());
//    };
}
