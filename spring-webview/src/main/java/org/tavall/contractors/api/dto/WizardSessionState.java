package org.tavall.contractors.api.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class WizardSessionState {

    private String sessionKey;
    private Long userId;
    private IntakeStatePayload currentIntake;
    private List<IntakeStatePayload> cart = new ArrayList<>();
    private List<CartAddonPayload> addons = new ArrayList<>();
    private Instant updatedAt = Instant.now();

    public String getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public IntakeStatePayload getCurrentIntake() {
        return currentIntake;
    }

    public void setCurrentIntake(IntakeStatePayload currentIntake) {
        this.currentIntake = currentIntake;
    }

    public List<IntakeStatePayload> getCart() {
        return cart;
    }

    public void setCart(List<IntakeStatePayload> cart) {
        this.cart = cart;
    }

    public List<CartAddonPayload> getAddons() {
        return addons;
    }

    public void setAddons(List<CartAddonPayload> addons) {
        this.addons = addons;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
