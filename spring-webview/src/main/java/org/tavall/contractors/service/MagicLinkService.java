package org.tavall.contractors.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.tavall.contractors.cache.WizardStateCache;
import org.tavall.contractors.domain.jpa.UserAccount;
import org.tavall.contractors.domain.jpa.UserRole;
import org.tavall.contractors.repo.jpa.UserAccountRepository;

import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class MagicLinkService {

    private static final Logger LOG = LoggerFactory.getLogger(MagicLinkService.class);
    private final WizardStateCache wizardStateCache;
    private final UserAccountRepository userAccountRepository;

    public MagicLinkService(WizardStateCache wizardStateCache, UserAccountRepository userAccountRepository) {
        this.wizardStateCache = wizardStateCache;
        this.userAccountRepository = userAccountRepository;
    }

    public void requestOtp(String email) {
        String normalized = normalizeEmail(email);
        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 999999));
        wizardStateCache.putOtp(normalized, otp, Duration.ofMinutes(10));
        LOG.info("[MAGIC-LINK] Simulated email send. otp={}, email={}", otp, normalized);
    }

    public UserAccount verifyOtp(String email, String otp) {
        String normalized = normalizeEmail(email);
        if (!wizardStateCache.verifyAndConsumeOtp(normalized, otp)) {
            throw new IllegalArgumentException("Invalid or expired OTP");
        }

        return userAccountRepository.findByEmailIgnoreCase(normalized)
            .orElseGet(() -> {
                UserAccount created = new UserAccount();
                created.setEmail(normalized);
                created.setDisplayName(normalized.split("@")[0]);
                created.setPrimaryRole(UserRole.CLIENT);
                return userAccountRepository.save(created);
            });
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}