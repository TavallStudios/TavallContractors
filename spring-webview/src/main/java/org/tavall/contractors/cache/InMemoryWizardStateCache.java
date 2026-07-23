package org.tavall.contractors.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.tavall.abstractcache.cache.AbstractCache;
import org.tavall.abstractcache.cache.enums.CacheDomain;
import org.tavall.abstractcache.cache.enums.CacheSource;
import org.tavall.abstractcache.cache.enums.CacheType;
import org.tavall.abstractcache.cache.enums.CacheVersion;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.tavall.contractors.api.dto.WizardSessionState;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class InMemoryWizardStateCache extends AbstractCache<String, Object> implements WizardStateCache {

    private static final String STATE_PREFIX = "wizard-state:";
    private static final String OTP_PREFIX = "wizard-otp:";

    private final Set<String> stateKeys = ConcurrentHashMap.newKeySet();
    private final Set<String> otpKeys = ConcurrentHashMap.newKeySet();
    private final ObjectMapper objectMapper;

    public InMemoryWizardStateCache(ObjectMapper objectMapper) {
        super(5, TimeUnit.MINUTES);
        this.objectMapper = objectMapper;
    }

    public CacheType getCacheType() {
        return CacheType.MEMORY;
    }

    public CacheDomain getCacheDomain() {
        return CacheDomain.USER;
    }

    public CacheSource getSource() {
        return CacheSource.USER_ACCOUNT_SERVICE;
    }

    public CacheVersion getVersion() {
        return CacheVersion.V1_0;
    }

    @Override
    public void putState(String sessionKey, WizardSessionState state, Duration ttl) {
        String rawKey = STATE_PREFIX + sessionKey;
        put(cacheKey(rawKey), deepCopy(state), ttlMillis(ttl));
        stateKeys.add(rawKey);
    }

    @Override
    public Optional<WizardSessionState> getState(String sessionKey) {
        String rawKey = STATE_PREFIX + sessionKey;
        Optional<WizardSessionState> value = resolveValue(rawKey, WizardSessionState.class, stateKeys);
        return value.map(this::deepCopy);
    }

    @Override
    public void evictState(String sessionKey) {
        String rawKey = STATE_PREFIX + sessionKey;
        removeValue(rawKey);
        stateKeys.remove(rawKey);
    }

    @Override
    public void putOtp(String email, String otp, Duration ttl) {
        String rawKey = OTP_PREFIX + normalizeEmail(email);
        put(cacheKey(rawKey), otp, ttlMillis(ttl));
        otpKeys.add(rawKey);
    }

    @Override
    public boolean verifyAndConsumeOtp(String email, String otp) {
        String rawKey = OTP_PREFIX + normalizeEmail(email);
        Optional<String> value = resolveValue(rawKey, String.class, otpKeys);
        if (value.isEmpty()) {
            return false;
        }
        boolean valid = value.get().equals(otp);
        if (valid) {
            removeValue(rawKey);
            otpKeys.remove(rawKey);
        }
        return valid;
    }

    @Scheduled(fixedDelay = 60_000L)
    @Override
    public int cleanupExpired() {
        int removed = super.cleanupExpired();
        stateKeys.removeIf(key -> !containsValue(key));
        otpKeys.removeIf(key -> !containsValue(key));
        return removed;
    }

    private WizardSessionState deepCopy(WizardSessionState state) {
        return objectMapper.convertValue(state, WizardSessionState.class);
    }

    private <T> Optional<T> resolveValue(String rawKey, Class<T> type, Set<String> index) {
        Object rawValue = getIfPresent(
                rawKey,
                getCacheDomain(),
                getCacheType(),
                getVersion(),
                getSource()
        );
        if (rawValue == null) {
            index.remove(rawKey);
            return Optional.empty();
        }
        if (!type.isInstance(rawValue)) {
            return Optional.empty();
        }
        return Optional.of(type.cast(rawValue));
    }

    private org.tavall.abstractcache.cache.interfaces.ICacheKey<String> cacheKey(String rawKey) {
        return createKey(rawKey, getCacheType(), getCacheDomain(), getSource(), getVersion());
    }

    private long ttlMillis(Duration ttl) {
        Duration safeTtl = ttl == null || ttl.isNegative() ? Duration.ZERO : ttl;
        return safeTtl.toMillis();
    }

    private boolean containsValue(String rawKey) {
        return containsKey(rawKey, getCacheDomain(), getCacheType(), getVersion(), getSource());
    }

    private void removeValue(String rawKey) {
        remove(rawKey, getCacheDomain(), getCacheType(), getVersion(), getSource());
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
