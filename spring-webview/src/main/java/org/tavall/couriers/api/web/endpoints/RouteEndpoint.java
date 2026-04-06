package org.tavall.couriers.api.web.endpoints;

public interface RouteEndpoint {

    String path();

    default String template() {
        return path();
    }

    static String sanitize(String path) {
        if (path == null) {
            return null;
        }
        return path.startsWith("#") ? path.substring(1) : path;
    }
}
