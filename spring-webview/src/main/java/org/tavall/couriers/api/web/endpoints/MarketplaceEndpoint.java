package org.tavall.couriers.api.web.endpoints;

public enum MarketplaceEndpoint implements RouteEndpoint {
    HIRE_DIRECT_TALENT(Routes.Api.Marketplace.HIRE_DIRECT_TALENT),
    PORTFOLIOS(Routes.Api.Marketplace.PORTFOLIOS),
    FREELANCER_JOBS(Routes.Api.Marketplace.FREELANCER_JOBS),
    CLIENT_DASHBOARD(Routes.Api.Marketplace.CLIENT_DASHBOARD),
    FREELANCER_DASHBOARD(Routes.Api.Marketplace.FREELANCER_DASHBOARD),
    ADMIN_STATS(Routes.Api.Marketplace.ADMIN_STATS);

    private final String path;

    MarketplaceEndpoint(String path) {
        this.path = RouteEndpoint.sanitize(path);
    }

    @Override
    public String path() {
        return path;
    }
}
