package org.tavall.contractors.service;

import org.springframework.stereotype.Service;
import org.tavall.contractors.domain.mongo.ProjectScopeDocument;
import org.tavall.contractors.repo.jpa.TalentProfileRepository;
import org.tavall.contractors.repo.jpa.UserAccountRepository;
import org.tavall.contractors.repo.mongo.ProjectScopeRepository;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class MarketplaceReadService {

    private final TalentProfileRepository talentProfileRepository;
    private final UserAccountRepository userAccountRepository;
    private final ProjectScopeRepository projectScopeRepository;

    public MarketplaceReadService(
        TalentProfileRepository talentProfileRepository,
        UserAccountRepository userAccountRepository,
        ProjectScopeRepository projectScopeRepository
    ) {
        this.talentProfileRepository = talentProfileRepository;
        this.userAccountRepository = userAccountRepository;
        this.projectScopeRepository = projectScopeRepository;
    }

    public List<TalentCard> talentDirectory() {
        return talentProfileRepository.findAllTalentProfiles().stream()
            .map(profile -> new TalentCard(
                profile.getUser().getId(),
                profile.getUser().getDisplayName(),
                profile.getHeadline(),
                profile.getHourlyRate(),
                profile.getSkills().stream().sorted().toList()
            ))
            .toList();
    }

    public List<PortfolioCard> completedPortfolios() {
        return projectScopeRepository.aggregateCompletedByDomain().stream()
            .map(item -> new PortfolioCard(item.getDomain(), item.getTotalProjects(), item.getCoverImage()))
            .toList();
    }

    public List<JobBoardItem> fundedJobsForTalent() {
        return projectScopeRepository.findByStatusAndTalentIdIsNull("FUNDED").stream()
            .map(doc -> new JobBoardItem(
                doc.getId(),
                doc.getTitle(),
                doc.getDomain(),
                doc.getEscrowBalance(),
                doc.getGeneratedSpecMarkdown()
            ))
            .toList();
    }

    public ClientDashboard clientDashboard(Long currentUserId) {
        List<ProjectScopeDocument> projects = projectScopeRepository.findByClientId(currentUserId);
        BigDecimal totalBudget = projects.stream()
            .map(ProjectScopeDocument::getTotalBudget)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal paidBudget = projects.stream()
            .map(ProjectScopeDocument::getPaidBudget)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        Optional<ProjectScopeDocument.Milestone> nextMilestone = projects.stream()
            .flatMap(p -> p.getMilestones().stream())
            .filter(m -> !"PAID".equals(m.getStatus()))
            .min(Comparator.comparing(ProjectScopeDocument.Milestone::getId));

        return new ClientDashboard(projects, paidBudget, totalBudget, nextMilestone.orElse(null));
    }

    public FreelancerDashboard freelancerDashboard(Long currentUserId) {
        List<ProjectScopeDocument> projects = projectScopeRepository.findByTalentId(currentUserId);
        BigDecimal availablePayout = projects.stream()
            .flatMap(p -> p.getMilestones().stream())
            .filter(m -> "REVIEW".equals(m.getStatus()) || "APPROVED".equals(m.getStatus()))
            .map(ProjectScopeDocument.Milestone::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new FreelancerDashboard(projects, availablePayout);
    }

    public AdminStats adminStats() {
        long users = userAccountRepository.count();
        BigDecimal activeEscrow = projectScopeRepository.sumActiveEscrow().stream()
            .map(ProjectScopeRepository.EscrowSumAggregate::getTotal)
            .findFirst()
            .orElse(BigDecimal.ZERO);
        long disputedProjects = projectScopeRepository.countByStatus("DISPUTED");
        return new AdminStats(users, activeEscrow, disputedProjects);
    }

    public record TalentCard(Long userId, String name, String headline, BigDecimal hourlyRate, List<String> skills) {
    }

    public record PortfolioCard(String domain, Integer totalProjects, String coverImage) {
    }

    public record JobBoardItem(String projectId, String title, String domain, BigDecimal escrowBalance, String specPreview) {
    }

    public record ClientDashboard(
        List<ProjectScopeDocument> projects,
        BigDecimal paidBudget,
        BigDecimal totalBudget,
        ProjectScopeDocument.Milestone nextActiveMilestone
    ) {
    }

    public record FreelancerDashboard(List<ProjectScopeDocument> projects, BigDecimal availablePayout) {
    }

    public record AdminStats(long users, BigDecimal activeEscrow, long disputedProjects) {
    }
}