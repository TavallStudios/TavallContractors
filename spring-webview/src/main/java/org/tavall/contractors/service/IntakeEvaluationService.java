package org.tavall.contractors.service;

import org.springframework.stereotype.Service;
import org.tavall.contractors.api.dto.IntakeStatePayload;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class IntakeEvaluationService {

    public EvaluationResult evaluate(IntakeStatePayload state) {
        int completeness = calculateCompleteness(state);
        int riskScore = calculateRiskScore(state);

        String risk = toRiskLabel(riskScore);
        List<String> badges = buildBadges(state, completeness, riskScore);

        state.setCompleteness(completeness);
        state.setRisk(risk);
        return new EvaluationResult(completeness, risk, badges, state);
    }

    private int calculateCompleteness(IntakeStatePayload state) {
        int score = 0;
        if (hasValue(state.getDomain())) score += 15;
        if (hasValue(state.getDomainType())) score += 10;
        if (hasValue(state.getTechLevel())) score += 10;
        if (hasValue(state.getBudget())) score += 15;
        if (state.getDocsType() != null && !state.getDocsType().isEmpty()) score += 10;
        if (state.getGuidedAnswers() != null && !state.getGuidedAnswers().isEmpty()) score += 20;
        if (hasValue(state.getContractType())) score += 10;
        if (hasValue(state.getDeadline())) score += 10;
        return Math.min(100, score);
    }

    private int calculateRiskScore(IntakeStatePayload state) {
        int risk = 35;
        String budget = safe(state.getBudget());
        String techLevel = safe(state.getTechLevel());
        String deadline = safe(state.getDeadline());

        if ("no_budget".equals(budget) || "no_budget_quote".equals(budget)) risk += 30;
        if ("custom".equals(budget) && !hasValue(state.getCustomBudget())) risk += 15;

        if (state.getDocsType() == null || state.getDocsType().isEmpty() || state.getDocsType().contains("none")) {
            risk += 20;
        }

        if ("dev".equals(techLevel) || "very_tech".equals(techLevel)) risk -= 12;
        if ("non_tech".equals(techLevel)) risk += 8;

        if (state.getGuidedAnswers() == null || state.getGuidedAnswers().isEmpty()) {
            risk += 20;
        } else {
            int answerDensity = countAnswers(state.getGuidedAnswers());
            risk -= Math.min(15, answerDensity * 2);
        }

        if ("urgent".equals(deadline)) risk += 12;
        return Math.max(0, Math.min(100, risk));
    }

    private int countAnswers(Map<String, Object> guidedAnswers) {
        int count = 0;
        for (Object value : guidedAnswers.values()) {
            if (value instanceof List<?> list) {
                count += list.size();
            } else if (value instanceof Map<?, ?> map) {
                count += map.size();
            } else if (value != null) {
                count++;
            }
        }
        return count;
    }

    private List<String> buildBadges(IntakeStatePayload state, int completeness, int riskScore) {
        List<String> badges = new ArrayList<>();
        if (riskScore >= 80) badges.add("High Scope Ambiguity");
        if (riskScore <= 35) badges.add("Low-Risk Spec");
        if (completeness >= 85) badges.add("Execution Ready");
        if (state.getDocsType() != null && state.getDocsType().contains("repo")) badges.add("Codebase Attached");
        return badges;
    }

    private boolean hasValue(String value) {
        return value != null && !value.isBlank();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String toRiskLabel(int riskScore) {
        if (riskScore >= 75) return "Critical";
        if (riskScore >= 45) return "Med";
        return "Low";
    }

    public record EvaluationResult(int completeness, String risk, List<String> badges, IntakeStatePayload state) {
    }
}