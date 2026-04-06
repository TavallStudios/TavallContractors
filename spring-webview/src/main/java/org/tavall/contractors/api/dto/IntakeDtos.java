package org.tavall.contractors.api.dto;

import java.math.BigDecimal;
import java.util.List;

public final class IntakeDtos {

    private IntakeDtos() {
    }

    public record EvaluateRequest(IntakeStatePayload state) {
    }

    public record EvaluateResponse(
        IntakeStatePayload state,
        int completeness,
        String risk,
        List<String> badges
    ) {
    }

    public record SpecRequest(IntakeStatePayload state) {
    }

    public record SpecResponse(String markdown) {
    }

    public record CheckoutRequest(String sessionKey) {
    }

    public record AddToCartRequest(IntakeStatePayload scope) {
    }

    public record SetAddonsRequest(List<CartAddonPayload> addons) {
    }

    public record CheckoutResponse(
        Long checkoutId,
        int projectCount,
        BigDecimal totalBudget,
        int addonCount,
        BigDecimal addonTotal,
        BigDecimal grandTotal,
        String status
    ) {
    }
}
