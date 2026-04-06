package org.tavall.contractors.repo.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tavall.contractors.domain.jpa.AuthProvider;
import org.tavall.contractors.domain.jpa.OAuthIdentity;

import java.util.Optional;

public interface OAuthIdentityRepository extends JpaRepository<OAuthIdentity, Long> {
    Optional<OAuthIdentity> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId);
}