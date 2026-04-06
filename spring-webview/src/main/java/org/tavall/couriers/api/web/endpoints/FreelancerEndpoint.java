package org.tavall.couriers.api.web.endpoints;

public enum FreelancerEndpoint implements RouteEndpoint {
    PORTFOLIO(Routes.Api.Freelancer.PORTFOLIO),
    WORKSPACE(Routes.Api.Freelancer.WORKSPACE),
    WORKSPACE_ACTIVATE(Routes.Api.Freelancer.WORKSPACE_ACTIVATE);

    private final String path;

    FreelancerEndpoint(String path) {
        this.path = RouteEndpoint.sanitize(path);
    }

    @Override
    public String path() {
        return path;
    }
}
