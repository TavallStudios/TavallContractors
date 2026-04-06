package org.tavall.contractors.cache;

import org.tavall.contractors.api.dto.WizardSessionState;

import java.time.Duration;
import java.util.Optional;

public interface WizardStateCache {
    void putState(String sessionKey, WizardSessionState state, Duration ttl);

    Optional<WizardSessionState> getState(String sessionKey);

    void evictState(String sessionKey);

    void putOtp(String email, String otp, Duration ttl);

    boolean verifyAndConsumeOtp(String email, String otp);
}