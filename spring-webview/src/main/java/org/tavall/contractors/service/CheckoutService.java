package org.tavall.contractors.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tavall.contractors.api.dto.CartAddonPayload;
import org.tavall.contractors.api.dto.IntakeDtos;
import org.tavall.contractors.api.dto.IntakeStatePayload;
import org.tavall.contractors.api.dto.WizardSessionState;
import org.tavall.contractors.cache.WizardStateCache;
import org.tavall.contractors.domain.jpa.CheckoutRecord;
import org.tavall.contractors.domain.jpa.UserAccount;
import org.tavall.contractors.domain.jpa.UserRole;
import org.tavall.contractors.domain.mongo.ProjectScopeDocument;
import org.tavall.contractors.repo.jpa.CheckoutRecordRepository;
import org.tavall.contractors.repo.jpa.UserAccountRepository;
import org.tavall.contractors.repo.mongo.ProjectScopeRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CheckoutService {

    private final WizardStateCache wizardStateCache;
    private final UserAccountRepository userAccountRepository;
    private final CheckoutRecordRepository checkoutRecordRepository;
    private final ProjectScopeRepository projectScopeRepository;
    private final IntakeEvaluationService intakeEvaluationService;
    private final TechSpecGeneratorService techSpecGeneratorService;

    public CheckoutService(
        WizardStateCache wizardStateCache,
        UserAccountRepository userAccountRepository,
        CheckoutRecordRepository checkoutRecordRepository,
        ProjectScopeRepository projectScopeRepository,
        IntakeEvaluationService intakeEvaluationService,
        TechSpecGeneratorService techSpecGeneratorService
    ) {
        this.wizardStateCache = wizardStateCache;
        this.userAccountRepository = userAccountRepository;
        this.checkoutRecordRepository = checkoutRecordRepository;
        this.projectScopeRepository = projectScopeRepository;
        this.intakeEvaluationService = intakeEvaluationService;
        this.techSpecGeneratorService = techSpecGeneratorService;
    }

    @Transactional
    public IntakeDtos.CheckoutResponse checkout(Long userId, IntakeDtos.CheckoutRequest request) {
        WizardSessionState state = wizardStateCache.getState(request.sessionKey())
            .orElseThrow(() -> new IllegalArgumentException("No cached wizard state found"));

        UserAccount user = resolveCheckoutUser(userId, request.sessionKey());

        List<IntakeStatePayload> scopes = new ArrayList<>();
        if (state.getCart() != null) scopes.addAll(state.getCart());
        if (state.getCurrentIntake() != null) scopes.add(state.getCurrentIntake());
        if (scopes.isEmpty()) {
            throw new IllegalStateException("Checkout blocked: no scoped projects in cache");
        }

        BigDecimal totalBudget = scopes.stream()
            .map(this::resolveBudgetValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        List<CartAddonPayload> addons = sanitizeAddons(state.getAddons());
        BigDecimal addonTotal = addons.stream()
            .map(this::resolveAddonPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal grandTotal = totalBudget.add(addonTotal);

        CheckoutRecord checkout = new CheckoutRecord();
        checkout.setUser(user);
        checkout.setSessionKey(request.sessionKey());
        checkout.setProjectCount(scopes.size());
        checkout.setTotalBudget(totalBudget);
        checkout.setAddonCount(addons.size());
        checkout.setAddonTotal(addonTotal);
        checkout.setGrandTotal(grandTotal);
        checkout = checkoutRecordRepository.save(checkout);

        List<String> insertedIds = new ArrayList<>();
        try {
            for (IntakeStatePayload scope : scopes) {
                intakeEvaluationService.evaluate(scope);
                ProjectScopeDocument document = new ProjectScopeDocument();
                BigDecimal budget = resolveBudgetValue(scope);

                document.setCheckoutId(checkout.getId());
                document.setClientId(user.getId());
                document.setTitle(scope.getDomainType() != null ? scope.getDomainType() : "Scoped Project");
                document.setDomain(scope.getDomain());
                document.setStatus("FUNDED");
                document.setTotalBudget(budget);
                document.setPaidBudget(BigDecimal.ZERO);
                document.setEscrowBalance(budget);
                document.setGeneratedSpecMarkdown(techSpecGeneratorService.generateMarkdown(scope));
                document.setGuidedAnswers(scope.getGuidedAnswers());
                document.setIntakeSnapshot(scopeSnapshot(scope, addons));
                document.setMilestones(defaultMilestones(budget));

                ProjectScopeDocument inserted = projectScopeRepository.save(document);
                insertedIds.add(inserted.getId());
            }
        } catch (RuntimeException ex) {
            if (!insertedIds.isEmpty()) {
                projectScopeRepository.deleteAllById(insertedIds);
            }
            throw ex;
        }

        wizardStateCache.evictState(request.sessionKey());
        return new IntakeDtos.CheckoutResponse(
            checkout.getId(),
            scopes.size(),
            totalBudget,
            addons.size(),
            addonTotal,
            grandTotal,
            "FUNDED"
        );
    }

    private List<ProjectScopeDocument.Milestone> defaultMilestones(BigDecimal budget) {
        BigDecimal m1 = percentage(budget, 0.25);
        BigDecimal m2 = percentage(budget, 0.50);
        BigDecimal m3 = budget.subtract(m1).subtract(m2);
        return List.of(
            new ProjectScopeDocument.Milestone("m1", "Scope + Architecture", m1, "LOCKED"),
            new ProjectScopeDocument.Milestone("m2", "Implementation", m2, "LOCKED"),
            new ProjectScopeDocument.Milestone("m3", "QA + Handover", m3, "LOCKED")
        );
    }

    private BigDecimal percentage(BigDecimal value, double ratio) {
        return value.multiply(BigDecimal.valueOf(ratio)).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal resolveBudgetValue(IntakeStatePayload scope) {
        String budget = scope.getBudget();
        if (budget == null) return BigDecimal.ZERO;
        return switch (budget) {
            case "1000-3000" -> BigDecimal.valueOf(2000);
            case "3000-10000" -> BigDecimal.valueOf(6500);
            case "10000+" -> BigDecimal.valueOf(15000);
            case "custom" -> parseCurrency(scope.getCustomBudget());
            case "no_budget", "no_budget_quote" -> BigDecimal.ZERO;
            default -> parseCurrency(budget);
        };
    }

    private BigDecimal parseCurrency(String value) {
        if (value == null || value.isBlank()) return BigDecimal.ZERO;
        String normalized = value.replaceAll("[^0-9.]", "");
        if (normalized.isBlank()) return BigDecimal.ZERO;
        return new BigDecimal(normalized).setScale(2, RoundingMode.HALF_UP);
    }

    private UserAccount resolveCheckoutUser(Long userId, String sessionKey) {
        if (userId != null) {
            return userAccountRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        }

        String guestEmail = "guest+" + sessionKey + "@tavall.local";
        return userAccountRepository.findByEmailIgnoreCase(guestEmail)
            .orElseGet(() -> {
                UserAccount guest = new UserAccount();
                guest.setEmail(guestEmail);
                guest.setDisplayName("Guest Checkout");
                guest.setPrimaryRole(UserRole.CLIENT);
                return userAccountRepository.save(guest);
            });
    }

    private BigDecimal resolveAddonPrice(CartAddonPayload addon) {
        if (addon == null || addon.getPrice() == null) {
            return BigDecimal.ZERO;
        }
        return addon.getPrice().max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    private List<CartAddonPayload> sanitizeAddons(List<CartAddonPayload> addons) {
        if (addons == null || addons.isEmpty()) {
            return List.of();
        }
        return addons.stream()
            .filter(item -> item != null && item.getCode() != null && !item.getCode().isBlank())
            .sorted(Comparator.comparing(CartAddonPayload::getCode))
            .toList();
    }

    private Map<String, Object> scopeSnapshot(IntakeStatePayload scope, List<CartAddonPayload> addons) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("domain", scope.getDomain());
        snapshot.put("domainType", scope.getDomainType());
        snapshot.put("domainSubtype", scope.getDomainSubtype());
        snapshot.put("language", scope.getLanguage());
        snapshot.put("techLevel", scope.getTechLevel());
        snapshot.put("budget", scope.getBudget());
        snapshot.put("customBudget", scope.getCustomBudget());
        snapshot.put("deadline", scope.getDeadline());
        snapshot.put("docsType", scope.getDocsType());
        snapshot.put("contractType", scope.getContractType());
        snapshot.put("completeness", scope.getCompleteness());
        snapshot.put("risk", scope.getRisk());
        snapshot.put("addons", addons);
        return snapshot;
    }
}
