package org.tavall.couriers.api.web.endpoints;

public enum AuthEndpoint implements RouteEndpoint {
    MAGIC_REQUEST(Routes.Api.Auth.MAGIC_REQUEST),
    MAGIC_VERIFY(Routes.Api.Auth.MAGIC_VERIFY);

    private final String path;

    AuthEndpoint(String path) {
        this.path = RouteEndpoint.sanitize(path);
    }

    @Override
    public String path() {

        return path;
    }
}
