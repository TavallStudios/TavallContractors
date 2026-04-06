package org.tavall.couriers.api.web.endpoints;

public enum PageEndpoint implements RouteEndpoint {
    HOME(Routes.Page.HOME),
    HIRE_DIRECT(Routes.Page.HIRE_DIRECT),
    PORTFOLIOS(Routes.Page.PORTFOLIOS),
    FREELANCER(Routes.Page.FREELANCER),
    CLIENT_DASHBOARD(Routes.Page.CLIENT_DASHBOARD),
    FREELANCER_DASHBOARD(Routes.Page.FREELANCER_DASHBOARD),
    FREELANCER_PORTFOLIO(Routes.Page.FREELANCER_PORTFOLIO);

    private final String path;

    PageEndpoint(String path) {
        this.path = RouteEndpoint.sanitize(path);
    }

    @Override
    public String path() {
        return path;
    }
}
