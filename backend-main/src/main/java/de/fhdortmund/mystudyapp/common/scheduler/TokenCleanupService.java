package de.fhdortmund.mystudyapp.common.scheduler;

import java.time.Instant;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.fhdortmund.mystudyapp.identity.repository.PasswordResetTokenRepository;
import de.fhdortmund.mystudyapp.identity.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenCleanupService {

    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Scheduled(cron = "0 0 3 * * *") // Every day at 3:00 AM
    @Transactional
    public void purgeExpiredTokens() {
        Instant now = Instant.now();
        int v = verificationTokenRepository.deleteAllExpiredBefore(now);
        int p = passwordResetTokenRepository.deleteAllExpiredBefore(now);
        log.info("Purged {} expired verification tokens and {} password reset tokens", v, p);
    }
}