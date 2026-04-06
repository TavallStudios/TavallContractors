package org.tavall.contractors.api.marketplace;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tavall.couriers.api.web.endpoints.Routes;
import org.tavall.contractors.security.CurrentUserResolver;
import org.tavall.contractors.service.MarketplaceReadService;

@RestController
public class MarketplaceController {

    private final MarketplaceReadService marketplaceReadService;
    private final CurrentUserResolver currentUserResolver;

    public MarketplaceController(MarketplaceReadService marketplaceReadService, CurrentUserResolver currentUserResolver) {
        this.marketplaceReadService = marketplaceReadService;
        this.currentUserResolver = currentUserResolver;
    }

    @GetMapping(Routes.Api.Marketplace.HIRE_DIRECT_TALENT)
    public Object hireDirect() {
        return marketplaceReadService.talentDirectory();
    }

    @GetMapping(Routes.Api.Marketplace.PORTFOLIOS)
    public Object portfolios() {
        return marketplaceReadService.completedPortfolios();
    }

    @GetMapping(Routes.Api.Marketplace.FREELANCER_JOBS)
    public Object freelancerJobs() {
        return marketplaceReadService.fundedJobsForTalent();
    }

    @GetMapping(Routes.Api.Marketplace.CLIENT_DASHBOARD)
    public Object clientDashboard(Authentication authentication) {
        Long userId = currentUserResolver.resolveUserId(authentication);
        return marketplaceReadService.clientDashboard(userId);
    }

    @GetMapping(Routes.Api.Marketplace.FREELANCER_DASHBOARD)
    public Object freelancerDashboard(Authentication authentication) {
        Long userId = currentUserResolver.resolveUserId(authentication);
        return marketplaceReadService.freelancerDashboard(userId);
    }

    @GetMapping(Routes.Api.Marketplace.ADMIN_STATS)
    public Object adminStats() {
        return marketplaceReadService.adminStats();
    }
}
