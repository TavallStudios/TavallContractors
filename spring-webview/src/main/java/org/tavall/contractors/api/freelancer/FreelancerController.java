package org.tavall.contractors.api.freelancer;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.tavall.contractors.api.dto.FreelancerPortfolioDtos;
import org.tavall.contractors.security.CurrentUserResolver;
import org.tavall.contractors.service.FreelancerPortfolioService;
import org.tavall.couriers.api.web.endpoints.Routes;

@RestController
public class FreelancerController {

    private final FreelancerPortfolioService freelancerPortfolioService;
    private final CurrentUserResolver currentUserResolver;

    public FreelancerController(
        FreelancerPortfolioService freelancerPortfolioService,
        CurrentUserResolver currentUserResolver
    ) {
        this.freelancerPortfolioService = freelancerPortfolioService;
        this.currentUserResolver = currentUserResolver;
    }

    @GetMapping(Routes.Api.Freelancer.PORTFOLIO)
    public FreelancerPortfolioDtos.FreelancerPortfolioView publicPortfolio(@PathVariable Long userId) {
        return freelancerPortfolioService.publicPortfolio(userId);
    }

    @GetMapping(Routes.Api.Freelancer.WORKSPACE)
    public FreelancerPortfolioDtos.FreelancerWorkspace workspace(Authentication authentication) {
        Long userId = currentUserResolver.resolveUserId(authentication);
        return freelancerPortfolioService.workspace(userId);
    }

    @PostMapping(Routes.Api.Freelancer.WORKSPACE_ACTIVATE)
    public FreelancerPortfolioDtos.FreelancerWorkspace activate(Authentication authentication) {
        Long userId = currentUserResolver.resolveUserId(authentication);
        return freelancerPortfolioService.activate(userId);
    }

    @PutMapping(Routes.Api.Freelancer.WORKSPACE)
    public FreelancerPortfolioDtos.FreelancerWorkspace saveWorkspace(
        @RequestBody(required = false) FreelancerPortfolioDtos.FreelancerWorkspaceUpdate update,
        Authentication authentication
    ) {
        Long userId = currentUserResolver.resolveUserId(authentication);
        return freelancerPortfolioService.saveWorkspace(userId, update);
    }
}
