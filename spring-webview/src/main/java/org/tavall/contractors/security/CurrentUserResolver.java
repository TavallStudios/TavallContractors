package org.tavall.contractors.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.tavall.contractors.repo.jpa.UserAccountRepository;

import java.util.Optional;

@Component
public class CurrentUserResolver {

    private final UserAccountRepository userAccountRepository;

    public CurrentUserResolver(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    public Long resolveUserId(Authentication authentication) {
        if (authentication == null) {
            throw new IllegalStateException("Authenticated user required");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Long userId) {
            return userId;
        }
        if (principal instanceof String value && value.matches("\\d+")) {
            return Long.parseLong(value);
        }
        if (principal instanceof OAuth2User oauth2User) {
            String email = oauth2User.getAttribute("email");
            return lookupByEmail(email);
        }

        String name = authentication.getName();
        if (name != null && name.contains("@")) {
            return lookupByEmail(name);
        }

        throw new IllegalStateException("Unable to resolve authenticated user");
    }

    public Optional<Long> resolveUserIdIfPresent(Authentication authentication) {
        if (authentication == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(resolveUserId(authentication));
        } catch (IllegalStateException ex) {
            return Optional.empty();
        }
    }

    private Long lookupByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalStateException("Authenticated principal does not expose an email");
        }
        Optional<Long> userId = userAccountRepository.findByEmailIgnoreCase(email).map(user -> user.getId());
        return userId.orElseThrow(() -> new IllegalStateException("No user mapped to authenticated principal"));
    }
}
