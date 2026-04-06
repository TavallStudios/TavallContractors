package org.tavall.couriers.api.web.endpoints;

public enum IntakeEndpoint implements RouteEndpoint {
    STATE(Routes.Api.Intake.STATE),
    CART_ITEMS(Routes.Api.Intake.CART_ITEMS),
    CART_ITEM(Routes.Api.Intake.CART_ITEM),
    ADDONS(Routes.Api.Intake.ADDONS),
    EVALUATE(Routes.Api.Intake.EVALUATE),
    SPEC(Routes.Api.Intake.SPEC),
    CHECKOUT(Routes.Api.Intake.CHECKOUT);

    private final String path;

    IntakeEndpoint(String path) {
        this.path = RouteEndpoint.sanitize(path);
    }

    @Override
    public String path() {
        return path;
    }
}
