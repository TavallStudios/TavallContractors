package org.tavall.contractors.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.tavall.contractors.domain.jpa.AuthProvider;
import org.tavall.contractors.domain.jpa.OAuthIdentity;
import org.tavall.contractors.domain.jpa.UserAccount;
import org.tavall.contractors.domain.jpa.UserRole;
import org.tavall.contractors.repo.jpa.OAuthIdentityRepository;
import org.tavall.contractors.repo.jpa.UserAccountRepository;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class OAuth2LinkingUserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
    private final UserAccountRepository userAccountRepository;
    private final OAuthIdentityRepository oauthIdentityRepository;

    public OAuth2LinkingUserService(
        UserAccountRepository userAccountRepository,
        OAuthIdentityRepository oauthIdentityRepository
    ) {
        this.userAccountRepository = userAccountRepository;
        this.oauthIdentityRepository = oauthIdentityRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = delegate.loadUser(userRequest);
        Map<String, Object> source = oauth2User.getAttributes();
        Map<String, Object> attributes = new HashMap<>(source);

        String registrationId = userRequest.getClientRegistration().getRegistrationId().toUpperCase(Locale.ROOT);
        AuthProvider provider = AuthProvider.valueOf(registrationId);

        String providerUserId = String.valueOf(attributes.getOrDefault("sub", attributes.getOrDefault("id", "")));
        String email = String.valueOf(attributes.getOrDefault("email", ""));
        if (email.isBlank() && "GITHUB".equals(registrationId)) {
            String login = String.valueOf(attributes.getOrDefault("login", "github-user"));
            email = login + "@users.noreply.github.com";
            attributes.put("email", email);
        }

        String normalizedEmail = email.toLowerCase(Locale.ROOT);
        UserAccount user = userAccountRepository.findByEmailIgnoreCase(normalizedEmail)
            .orElseGet(() -> {
                UserAccount created = new UserAccount();
                created.setEmail(normalizedEmail);
                created.setDisplayName(String.valueOf(attributes.getOrDefault("name", normalizedEmail)));
                created.setPrimaryRole(UserRole.CLIENT);
                return userAccountRepository.save(created);
            });

        OAuthIdentity identity = oauthIdentityRepository
            .findByProviderAndProviderUserId(provider, providerUserId)
            .orElseGet(OAuthIdentity::new);
        identity.setProvider(provider);
        identity.setProviderUserId(providerUserId);
        identity.setProviderEmail(normalizedEmail);
        identity.setUser(user);
        oauthIdentityRepository.save(identity);

        Set<GrantedAuthority> authorities = Set.of(new SimpleGrantedAuthority("ROLE_" + user.getPrimaryRole().name()));
        return new DefaultOAuth2User(authorities, attributes, "email");
    }
}