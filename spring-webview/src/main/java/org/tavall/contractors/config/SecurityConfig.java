package org.tavall.contractors.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.tavall.couriers.api.web.endpoints.Routes;
import org.tavall.contractors.security.OAuth2LinkingUserService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, OAuth2LinkingUserService oAuth2LinkingUserService) throws Exception {
        http
            .csrf(csrf -> csrf.ignoringRequestMatchers(Routes.Api.ANY))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    Routes.Page.HOME,
                    Routes.Page.HIRE_DIRECT,
                    Routes.Page.PORTFOLIOS,
                    Routes.Page.FREELANCER,
                    Routes.Page.FREELANCER_PORTFOLIO_PATTERN,
                    Routes.Page.AUTH_ANY
                ).permitAll()
                .requestMatchers(Routes.Api.Auth.ANY).permitAll()
                .requestMatchers(
                    Routes.Api.Intake.STATE_ANY,
                    Routes.Api.Intake.CART_ANY,
                    Routes.Api.Intake.EVALUATE,
                    Routes.Api.Intake.SPEC,
                    Routes.Api.Intake.CHECKOUT
                ).permitAll()
                .requestMatchers(
                    HttpMethod.GET,
                    Routes.Api.Marketplace.HIRE_DIRECT_ANY,
                    Routes.Api.Marketplace.PORTFOLIOS_ANY,
                    Routes.Api.Freelancer.FREELANCERS_ANY
                ).permitAll()
                .requestMatchers(HttpMethod.GET, Routes.Api.Marketplace.FREELANCER_JOBS).permitAll()
                .requestMatchers(Routes.Api.Marketplace.ADMIN_ANY).hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth -> oauth
                .userInfoEndpoint(userInfo -> userInfo.userService(oAuth2LinkingUserService))
            )
            .oauth2Client(Customizer.withDefaults())
            .logout(logout -> logout.logoutSuccessUrl(Routes.Page.HOME))
            .formLogin(form -> form.disable());

        return http.build();
    }
}