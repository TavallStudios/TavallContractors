package org.tavall.contractors.api.dto;

import java.math.BigDecimal;
import java.util.List;

public final class FreelancerPortfolioDtos {

    private FreelancerPortfolioDtos() {
    }

    public record PortfolioProject(
        String title,
        String summary,
        String tech,
        String imageUrl
    ) {
    }

    public record PortfolioLink(
        String label,
        String url
    ) {
    }

    public record PortfolioContent(
        String tagline,
        String summary,
        String location,
        String availability,
        List<String> skills,
        List<PortfolioProject> projects,
        List<PortfolioLink> links
    ) {
    }

    public record FreelancerPortfolioView(
        Long userId,
        String displayName,
        String headline,
        BigDecimal hourlyRate,
        PortfolioContent portfolio
    ) {
    }

    public record TalentProfileSummary(
        String headline,
        BigDecimal hourlyRate,
        List<String> skills
    ) {
    }

    public record FreelancerWorkspace(
        Long userId,
        String role,
        boolean activated,
        String portfolioUrl,
        String displayName,
        TalentProfileSummary talentProfile,
        PortfolioContent portfolio
    ) {
    }

    public record FreelancerWorkspaceUpdate(
        String displayName,
        String headline,
        BigDecimal hourlyRate,
        List<String> skills,
        PortfolioContent portfolio
    ) {
    }
}