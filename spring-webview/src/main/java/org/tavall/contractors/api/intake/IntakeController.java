package org.tavall.contractors.api.intake;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.tavall.contractors.api.dto.CartAddonPayload;
import org.tavall.contractors.api.dto.IntakeStatePayload;
import org.tavall.contractors.api.dto.IntakeDtos;
import org.tavall.contractors.api.dto.WizardSessionState;
import org.tavall.contractors.cache.WizardStateCache;
import org.tavall.couriers.api.web.endpoints.Routes;
import org.tavall.contractors.security.CurrentUserResolver;
import org.tavall.contractors.service.CheckoutService;
import org.tavall.contractors.service.IntakeEvaluationService;
import org.tavall.contractors.service.TechSpecGeneratorService;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@RestController
public class IntakeController {

    private final WizardStateCache wizardStateCache;
    private final IntakeEvaluationService intakeEvaluationService;
    private final TechSpecGeneratorService techSpecGeneratorService;
    private final CheckoutService checkoutService;
    private final CurrentUserResolver currentUserResolver;

    public IntakeController(
        WizardStateCache wizardStateCache,
        IntakeEvaluationService intakeEvaluationService,
        TechSpecGeneratorService techSpecGeneratorService,
        CheckoutService checkoutService,
        CurrentUserResolver currentUserResolver
    ) {
        this.wizardStateCache = wizardStateCache;
        this.intakeEvaluationService = intakeEvaluationService;
        this.techSpecGeneratorService = techSpecGeneratorService;
        this.checkoutService = checkoutService;
        this.currentUserResolver = currentUserResolver;
    }

    @PutMapping(Routes.Api.Intake.STATE)
    public WizardSessionState putState(
        @PathVariable String sessionKey,
        @RequestBody WizardSessionState state,
        Authentication authentication
    ) {
        WizardSessionState merged = state == null ? emptyState(sessionKey) : state;
        merged.setSessionKey(sessionKey);
        currentUserResolver.resolveUserIdIfPresent(authentication).ifPresent(merged::setUserId);
        if (merged.getCurrentIntake() == null) {
            merged.setCurrentIntake(new IntakeStatePayload());
        }
        if (merged.getCart() == null) {
            merged.setCart(new ArrayList<>());
        }
        if (merged.getAddons() == null) {
            merged.setAddons(new ArrayList<>());
        }
        merged.setUpdatedAt(Instant.now());

        wizardStateCache.putState(sessionKey, merged, Duration.ofHours(6));
        return merged;
    }

    @PostMapping(Routes.Api.Intake.CART_ITEMS)
    public WizardSessionState addScopeToCart(
        @PathVariable String sessionKey,
        @RequestBody(required = false) IntakeDtos.AddToCartRequest request,
        Authentication authentication
    ) {
        WizardSessionState state = stateOrEmpty(sessionKey, authentication);

        IntakeStatePayload scope = request != null ? request.scope() : null;
        if (scope == null) {
            scope = state.getCurrentIntake();
        }
        if (scope == null) {
            throw new IllegalArgumentException("No intake payload to add");
        }

        IntakeStatePayload evaluated = intakeEvaluationService.evaluate(scope).state();
        state.getCart().add(evaluated);
        state.setUpdatedAt(Instant.now());
        wizardStateCache.putState(sessionKey, state, Duration.ofHours(6));
        return state;
    }

    @DeleteMapping(Routes.Api.Intake.CART_ITEM)
    public WizardSessionState removeScopeFromCart(
        @PathVariable String sessionKey,
        @PathVariable int index,
        Authentication authentication
    ) {
        WizardSessionState state = stateOrEmpty(sessionKey, authentication);
        if (index < 0 || index >= state.getCart().size()) {
            throw new IllegalArgumentException("Invalid cart index");
        }
        state.getCart().remove(index);
        state.setUpdatedAt(Instant.now());
        wizardStateCache.putState(sessionKey, state, Duration.ofHours(6));
        return state;
    }

    @PutMapping(Routes.Api.Intake.ADDONS)
    public WizardSessionState saveAddons(
        @PathVariable String sessionKey,
        @RequestBody IntakeDtos.SetAddonsRequest request,
        Authentication authentication
    ) {
        WizardSessionState state = stateOrEmpty(sessionKey, authentication);
        List<CartAddonPayload> addons = request == null ? List.of() : sanitizeAddons(request.addons());
        state.setAddons(new ArrayList<>(addons));
        state.setUpdatedAt(Instant.now());
        wizardStateCache.putState(sessionKey, state, Duration.ofHours(6));
        return state;
    }

    @GetMapping(Routes.Api.Intake.STATE)
    public WizardSessionState getState(@PathVariable String sessionKey) {
        return wizardStateCache.getState(sessionKey)
            .orElseThrow(() -> new IllegalArgumentException("No state found"));
    }

    @PostMapping(Routes.Api.Intake.EVALUATE)
    public IntakeDtos.EvaluateResponse evaluate(@RequestBody IntakeDtos.EvaluateRequest request) {
        var result = intakeEvaluationService.evaluate(request.state());
        return new IntakeDtos.EvaluateResponse(result.state(), result.completeness(), result.risk(), result.badges());
    }

    @PostMapping(Routes.Api.Intake.SPEC)
    public IntakeDtos.SpecResponse generateSpec(@RequestBody IntakeDtos.SpecRequest request) {
        String markdown = techSpecGeneratorService.generateMarkdown(request.state());
        return new IntakeDtos.SpecResponse(markdown);
    }

    @PostMapping(Routes.Api.Intake.CHECKOUT)
    public IntakeDtos.CheckoutResponse checkout(@RequestBody IntakeDtos.CheckoutRequest request, Authentication authentication) {
        Long userId = currentUserResolver.resolveUserIdIfPresent(authentication).orElse(null);
        return checkoutService.checkout(userId, request);
    }

    private WizardSessionState stateOrEmpty(String sessionKey, Authentication authentication) {
        return wizardStateCache.getState(sessionKey).orElseGet(() -> {
            WizardSessionState state = emptyState(sessionKey);
            currentUserResolver.resolveUserIdIfPresent(authentication).ifPresent(state::setUserId);
            wizardStateCache.putState(sessionKey, state, Duration.ofHours(6));
            return state;
        });
    }

    private WizardSessionState emptyState(String sessionKey) {
        WizardSessionState state = new WizardSessionState();
        state.setSessionKey(sessionKey);
        state.setCurrentIntake(new IntakeStatePayload());
        state.setCart(new ArrayList<>());
        state.setAddons(new ArrayList<>());
        state.setUpdatedAt(Instant.now());
        return state;
    }

    private List<CartAddonPayload> sanitizeAddons(List<CartAddonPayload> input) {
        if (input == null || input.isEmpty()) {
            return List.of();
        }
        List<CartAddonPayload> sanitized = new ArrayList<>();
        for (CartAddonPayload item : input) {
            if (item == null || item.getCode() == null || item.getCode().isBlank()) {
                continue;
            }
            CartAddonPayload payload = new CartAddonPayload();
            payload.setCode(item.getCode().trim());
            payload.setLabel(item.getLabel() == null ? item.getCode().trim() : item.getLabel().trim());
            payload.setDescription(item.getDescription() == null ? "" : item.getDescription().trim());
            BigDecimal price = item.getPrice() == null ? BigDecimal.ZERO : item.getPrice();
            payload.setPrice(price.max(BigDecimal.ZERO));
            sanitized.add(payload);
        }
        return sanitized;
    }
}
