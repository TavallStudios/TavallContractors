package org.tavall.contractors.api.auth;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.tavall.couriers.api.web.endpoints.Routes;
import org.springframework.web.bind.annotation.RestController;
import org.tavall.contractors.domain.jpa.UserAccount;
import org.tavall.contractors.service.MagicLinkService;

import java.util.List;
import java.util.Map;

@RestController
public class AuthController {

    private final MagicLinkService magicLinkService;

    public AuthController(MagicLinkService magicLinkService) {
        this.magicLinkService = magicLinkService;
    }

    @PostMapping(Routes.Api.Auth.MAGIC_REQUEST)
    public Map<String, String> requestOtp(@RequestBody MagicRequest request) {
        magicLinkService.requestOtp(request.email());
        return Map.of("status", "OTP_SENT");
    }

    @PostMapping(Routes.Api.Auth.MAGIC_VERIFY)
    public Map<String, Object> verifyOtp(@RequestBody MagicVerifyRequest request) {
        UserAccount user = magicLinkService.verifyOtp(request.email(), request.otp());
        var authentication = new UsernamePasswordAuthenticationToken(
            user.getId(),
            null,
            List.of(new SimpleGrantedAuthority("ROLE_" + user.getPrimaryRole().name()))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return Map.of(
            "status", "AUTHENTICATED",
            "userId", user.getId(),
            "email", user.getEmail(),
            "role", user.getPrimaryRole().name()
        );
    }

    public record MagicRequest(String email) {
    }

    public record MagicVerifyRequest(String email, String otp) {
    }
}
