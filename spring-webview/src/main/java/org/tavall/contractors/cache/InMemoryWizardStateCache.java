package org.tavall.contractors.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.tavall.abstractcache.cache.AbstractCache;
import org.tavall.abstractcache.cache.CacheValue;
import org.tavall.abstractcache.cache.enums.CacheDomain;
import org.tavall.abstractcache.cache.enums.CacheSource;
import org.tavall.abstractcache.cache.enums.CacheType;
import org.tavall.abstractcache.cache.enums.CacheVersion;
import org.tavall.abstractcache.cache.interfaces.ICacheKey;
import org.tavall.abstractcache.cache.interfaces.ICacheValue;
import org.tavall.abstractcache.cache.maps.CacheMap;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.tavall.contractors.api.dto.WizardSessionState;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryWizardStateCache extends AbstractCache<String, Object> implements WizardStateCache {

    private static final String STATE_PREFIX = "wizard-state:";
    private static final String OTP_PREFIX = "wizard-otp:";

    private final CacheMap cacheMap = CacheMap.getCacheMap();
    private final Set<String> stateKeys = ConcurrentHashMap.newKeySet();
    private final Set<String> otpKeys = ConcurrentHashMap.newKeySet();
    private final ObjectMapper objectMapper;

    public InMemoryWizardStateCache(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public CacheType getCacheType() {
        return CacheType.MEMORY;
    }

    @Override
    public CacheDomain getCacheDomain() {
        return CacheDomain.USER;
    }

    @Override
    public CacheSource getSource() {
        return CacheSource.USER_ACCOUNT_SERVICE;
    }

    @Override
    public CacheVersion getVersion() {
        return CacheVersion.V1_0;
    }

    @Override
    public void putState(String sessionKey, WizardSessionState state, Duration ttl) {
        String rawKey = STATE_PREFIX + sessionKey;
        ICacheKey<String> cacheKey = cacheKey(rawKey);
        cacheMap.removeCacheKey(cacheKey);
        cacheMap.add(cacheKey, createValue(deepCopy(state), expiresAt(ttl)));
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
        cacheMap.removeCacheKey(cacheKey(rawKey));
        stateKeys.remove(rawKey);
    }

    @Override
    public void putOtp(String email, String otp, Duration ttl) {
        String rawKey = OTP_PREFIX + normalizeEmail(email);
        ICacheKey<String> cacheKey = cacheKey(rawKey);
        cacheMap.removeCacheKey(cacheKey);
        cacheMap.add(cacheKey, createValue(otp, expiresAt(ttl)));
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
            cacheMap.removeCacheKey(cacheKey(rawKey));
            otpKeys.remove(rawKey);
        }
        return valid;
    }

    @Scheduled(fixedDelay = 60_000L)
    void cleanupExpired() {
        for (String key : stateKeys) {
            resolveValue(key, WizardSessionState.class, stateKeys);
        }
        for (String key : otpKeys) {
            resolveValue(key, String.class, otpKeys);
        }
    }

    private WizardSessionState deepCopy(WizardSessionState state) {
        return objectMapper.convertValue(state, WizardSessionState.class);
    }

    private <T> Optional<T> resolveValue(String rawKey, Class<T> type, Set<String> index) {
        List<ICacheValue<?>> bucket = cacheMap.getBucket(cacheKey(rawKey));
        if (bucket.isEmpty()) {
            index.remove(rawKey);
            return Optional.empty();
        }

        ICacheValue<?> wrapper = bucket.get(bucket.size() - 1);
        if (!(wrapper instanceof CacheValue<?> cacheValue)) {
            cacheMap.removeCacheKey(cacheKey(rawKey));
            index.remove(rawKey);
            return Optional.empty();
        }
        if (cacheValue.isExpired()) {
            cacheMap.removeCacheKey(cacheKey(rawKey));
            index.remove(rawKey);
            return Optional.empty();
        }

        Object rawValue = cacheValue.getValue();
        if (!type.isInstance(rawValue)) {
            return Optional.empty();
        }
        return Optional.of(type.cast(rawValue));
    }

    private ICacheKey<String> cacheKey(String rawKey) {
        return createKey(rawKey, getCacheType(), getCacheDomain(), getSource(), getVersion());
    }

    private long expiresAt(Duration ttl) {
        Duration safeTtl = ttl == null || ttl.isNegative() ? Duration.ZERO : ttl;
        return System.currentTimeMillis() + safeTtl.toMillis();
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
