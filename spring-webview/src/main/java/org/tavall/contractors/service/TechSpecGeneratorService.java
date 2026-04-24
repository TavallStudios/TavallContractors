package org.tavall.contractors.service;

import org.springframework.stereotype.Service;
import org.tavall.contractors.api.dto.IntakeStatePayload;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

@Service
public class TechSpecGeneratorService {

    public String generateMarkdown(IntakeStatePayload state) {
        StringBuilder markdown = new StringBuilder();
        markdown.append("# Tavall Technical Specification\n\n");
        markdown.append("## Project Summary\n");
        markdown.append("- Domain: ").append(orDefault(state.getDomain(), "Unspecified")).append("\n");
        markdown.append("- Type: ").append(orDefault(state.getDomainType(), "Unspecified")).append("\n");
        markdown.append("- Contract: ").append(orDefault(state.getContractType(), "Unspecified")).append("\n");
        markdown.append("- Budget: ").append(resolveBudgetLabel(state)).append("\n");
        markdown.append("- Deadline: ").append(orDefault(state.getDeadline(), "Flexible")).append("\n\n");

        markdown.append("## Functional Scope\n");
        appendGuidedAnswers(markdown, state.getGuidedAnswers(), 0);

        markdown.append("\n## Acceptance Criteria\n");
        markdown.append("- Requirements traceable to guided answers\n");
        markdown.append("- Milestones mapped to verifiable outputs\n");
        markdown.append("- Change requests require funded change-order\n");
        markdown.append("- 72h review window per milestone\n");

        markdown.append("\n## Risk Controls\n");
        markdown.append("- Scope lock before escrow release\n");
        markdown.append("- Escrow-funded milestones only\n");
        markdown.append("- Disputes reference this specification as source of truth\n");
        return markdown.toString();
    }

    private void appendGuidedAnswers(StringBuilder markdown, Map<String, Object> answers, int depth) {
        if (answers == null || answers.isEmpty()) {
            markdown.append("- No guided answers supplied.\n");
            return;
        }

        String indent = "  ".repeat(Math.max(0, depth));
        for (Map.Entry<String, Object> entry : answers.entrySet()) {
            markdown.append(indent).append("- ").append(humanize(entry.getKey())).append(": ");
            Object value = entry.getValue();
            if (value instanceof List<?> list) {
                StringJoiner joiner = new StringJoiner(", ");
                list.forEach(item -> joiner.add(String.valueOf(item)));
                markdown.append(joiner).append("\n");
            } else if (value instanceof Map<?, ?> nested) {
                markdown.append("\n");
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedMap = (Map<String, Object>) nested;
                appendGuidedAnswers(markdown, nestedMap, depth + 1);
            } else {
                markdown.append(String.valueOf(value)).append("\n");
            }
        }
    }

    private String resolveBudgetLabel(IntakeStatePayload state) {
        if ("custom".equals(state.getBudget()) && state.getCustomBudget() != null) {
            return state.getCustomBudget();
        }
        return orDefault(state.getBudget(), "Unspecified");
    }

    private String humanize(String value) {
        return value == null ? "Unknown" : value.replace("_", " ");
    }

    private String orDefault(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }
}