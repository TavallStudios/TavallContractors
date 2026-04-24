package org.tavall.contractors.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tavall.contractors.api.dto.FreelancerPortfolioDtos;
import org.tavall.contractors.domain.jpa.TalentProfile;
import org.tavall.contractors.domain.jpa.UserAccount;
import org.tavall.contractors.domain.jpa.UserRole;
import org.tavall.contractors.domain.mongo.DashboardConfig;
import org.tavall.contractors.repo.jpa.TalentProfileRepository;
import org.tavall.contractors.repo.jpa.UserAccountRepository;
import org.tavall.contractors.repo.mongo.DashboardConfigRepository;
import org.tavall.couriers.api.web.endpoints.Routes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class FreelancerPortfolioService {

    private static final BigDecimal DEFAULT_HOURLY_RATE = new BigDecimal("85.00");

    private final UserAccountRepository userAccountRepository;
    private final TalentProfileRepository talentProfileRepository;
    private final DashboardConfigRepository dashboardConfigRepository;

    public FreelancerPortfolioService(
        UserAccountRepository userAccountRepository,
        TalentProfileRepository talentProfileRepository,
        DashboardConfigRepository dashboardConfigRepository
    ) {
        this.userAccountRepository = userAccountRepository;
        this.talentProfileRepository = talentProfileRepository;
        this.dashboardConfigRepository = dashboardConfigRepository;
    }

    @Transactional
    public FreelancerPortfolioDtos.FreelancerPortfolioView publicPortfolio(Long userId) {
        UserAccount user = requireUser(userId);
        TalentProfile talentProfile = talentProfileRepository.findByUserId(userId).orElse(null);
        DashboardConfig config = upsertConfig(userId);
        DashboardConfig.FreelancerPortfolio normalized = normalizePortfolio(config.getPortfolio(), user, talentProfile);
        config.setPortfolio(normalized);
        config.setUpdatedAt(Instant.now());
        dashboardConfigRepository.save(config);

        String headline = talentProfile != null ? talentProfile.getHeadline() : normalized.getTagline();
        BigDecimal hourlyRate = talentProfile != null ? talentProfile.getHourlyRate() : DEFAULT_HOURLY_RATE;
        return new FreelancerPortfolioDtos.FreelancerPortfolioView(
            user.getId(),
            user.getDisplayName(),
            cleanString(headline, "Freelance contractor"),
            hourlyRate,
            toDto(normalized)
        );
    }

    public FreelancerPortfolioDtos.FreelancerWorkspace workspace(Long userId) {
        UserAccount user = requireUser(userId);
        TalentProfile talentProfile = talentProfileRepository.findByUserId(userId).orElse(null);
        DashboardConfig config = dashboardConfigRepository.findByUserId(userId).orElse(null);
        DashboardConfig.FreelancerPortfolio normalized = normalizePortfolio(
            config == null ? null : config.getPortfolio(),
            user,
            talentProfile
        );
        boolean activated = user.getPrimaryRole() == UserRole.TALENT;
        return new FreelancerPortfolioDtos.FreelancerWorkspace(
            user.getId(),
            user.getPrimaryRole().name(),
            activated,
            portfolioUrl(user.getId()),
            user.getDisplayName(),
            toTalentSummary(talentProfile, normalized),
            toDto(normalized)
        );
    }

    @Transactional
    public FreelancerPortfolioDtos.FreelancerWorkspace activate(Long userId) {
        UserAccount user = requireUser(userId);
        if (user.getPrimaryRole() != UserRole.TALENT) {
            user.setPrimaryRole(UserRole.TALENT);
            userAccountRepository.save(user);
        }

        TalentProfile talentProfile = talentProfileRepository.findByUserId(userId).orElseGet(() -> {
            TalentProfile created = new TalentProfile();
            created.setUser(user);
            created.setHeadline("Independent freelancer available for new contracts");
            created.setHourlyRate(DEFAULT_HOURLY_RATE);
            created.setSkills(new LinkedHashSet<>(List.of("Add your top skill", "Add your second skill")));
            return talentProfileRepository.save(created);
        });

        DashboardConfig config = upsertConfig(userId);
        config.setPortfolio(normalizePortfolio(config.getPortfolio(), user, talentProfile));
        config.setUpdatedAt(Instant.now());
        dashboardConfigRepository.save(config);

        return workspace(userId);
    }

    @Transactional
    public FreelancerPortfolioDtos.FreelancerWorkspace saveWorkspace(Long userId, FreelancerPortfolioDtos.FreelancerWorkspaceUpdate update) {
        UserAccount user = requireUser(userId);
        if (update != null && update.displayName() != null && !update.displayName().isBlank()) {
            user.setDisplayName(update.displayName().trim());
            userAccountRepository.save(user);
        }
        if (user.getPrimaryRole() != UserRole.TALENT) {
            user.setPrimaryRole(UserRole.TALENT);
            userAccountRepository.save(user);
        }

        TalentProfile talentProfile = talentProfileRepository.findByUserId(userId).orElseGet(() -> {
            TalentProfile created = new TalentProfile();
            created.setUser(user);
            return created;
        });

        String updatedHeadline = update == null ? null : update.headline();
        talentProfile.setHeadline(cleanString(updatedHeadline, "Independent freelancer available for new contracts"));
        BigDecimal updatedRate = update == null ? null : update.hourlyRate();
        talentProfile.setHourlyRate(updatedRate == null ? DEFAULT_HOURLY_RATE : updatedRate.max(BigDecimal.ZERO));

        List<String> providedSkills = update == null ? List.of() : normalizeSkills(update.skills());
        if (providedSkills.isEmpty() && update != null && update.portfolio() != null) {
            providedSkills = normalizeSkills(update.portfolio().skills());
        }
        if (providedSkills.isEmpty()) {
            providedSkills = List.of("Add your top skill", "Add your second skill");
        }
        talentProfile.setSkills(new LinkedHashSet<>(providedSkills));
        talentProfileRepository.save(talentProfile);

        DashboardConfig config = upsertConfig(userId);
        DashboardConfig.FreelancerPortfolio incomingPortfolio = fromDto(update == null ? null : update.portfolio());
        DashboardConfig.FreelancerPortfolio normalized = normalizePortfolio(incomingPortfolio, user, talentProfile);
        config.setPortfolio(normalized);
        config.setUpdatedAt(Instant.now());
        dashboardConfigRepository.save(config);

        return workspace(userId);
    }

    private UserAccount requireUser(Long userId) {
        return userAccountRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }

    private DashboardConfig upsertConfig(Long userId) {
        return dashboardConfigRepository.findByUserId(userId)
            .orElseGet(() -> {
                DashboardConfig config = new DashboardConfig();
                config.setUserId(userId);
                return config;
            });
    }

    private DashboardConfig.FreelancerPortfolio normalizePortfolio(
        DashboardConfig.FreelancerPortfolio source,
        UserAccount user,
        TalentProfile talentProfile
    ) {
        DashboardConfig.FreelancerPortfolio portfolio = source == null
            ? new DashboardConfig.FreelancerPortfolio()
            : source;

        String userName = cleanString(user.getDisplayName(), "Freelancer");
        String headline = cleanString(talentProfile == null ? null : talentProfile.getHeadline(), "Independent contractor");

        DashboardConfig.FreelancerPortfolio normalized = new DashboardConfig.FreelancerPortfolio();
        normalized.setTagline(cleanString(portfolio.getTagline(), userName + " | " + headline));
        normalized.setSummary(cleanString(
            portfolio.getSummary(),
            "Write a short summary of your strongest experience, delivery style, and results."
        ));
        normalized.setLocation(cleanString(portfolio.getLocation(), "Remote / Your city"));
        normalized.setAvailability(cleanString(portfolio.getAvailability(), "Open for contract projects"));

        List<String> skills = normalizeSkills(portfolio.getSkills());
        if (skills.isEmpty() && talentProfile != null) {
            skills = normalizeSkills(new ArrayList<>(talentProfile.getSkills()));
        }
        if (skills.isEmpty()) {
            skills = List.of("Primary skill", "Secondary skill", "Tooling");
        }
        normalized.setSkills(skills);

        List<DashboardConfig.ProjectShowcase> projects = normalizeProjects(portfolio.getProjects());
        if (projects.isEmpty()) {
            projects = defaultProjects();
        }
        normalized.setProjects(projects);

        List<DashboardConfig.PortfolioLink> links = normalizeLinks(portfolio.getLinks());
        if (links.isEmpty()) {
            links = defaultLinks();
        }
        normalized.setLinks(links);
        return normalized;
    }

    private List<String> normalizeSkills(List<String> rawSkills) {
        if (rawSkills == null) {
            return List.of();
        }
        return rawSkills.stream()
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(value -> !value.isBlank())
            .distinct()
            .limit(24)
            .toList();
    }

    private List<DashboardConfig.ProjectShowcase> normalizeProjects(List<DashboardConfig.ProjectShowcase> projects) {
        if (projects == null) {
            return List.of();
        }
        return projects.stream()
            .filter(Objects::nonNull)
            .map(project -> {
                DashboardConfig.ProjectShowcase normalized = new DashboardConfig.ProjectShowcase();
                normalized.setTitle(cleanString(project.getTitle(), "Project title"));
                normalized.setSummary(cleanString(project.getSummary(), "Describe business context, your approach, and delivery impact."));
                normalized.setTech(cleanString(project.getTech(), "Tech stack"));
                normalized.setImageUrl(cleanString(
                    project.getImageUrl(),
                    "https://images.unsplash.com/photo-1518770660439-4636190af475?q=80&w=1200&auto=format&fit=crop"
                ));
                return normalized;
            })
            .limit(12)
            .toList();
    }

    private List<DashboardConfig.PortfolioLink> normalizeLinks(List<DashboardConfig.PortfolioLink> links) {
        if (links == null) {
            return List.of();
        }
        return links.stream()
            .filter(Objects::nonNull)
            .map(link -> {
                DashboardConfig.PortfolioLink normalized = new DashboardConfig.PortfolioLink();
                normalized.setLabel(cleanString(link.getLabel(), "Profile link"));
                normalized.setUrl(cleanString(link.getUrl(), "#"));
                return normalized;
            })
            .limit(8)
            .toList();
    }

    private List<DashboardConfig.ProjectShowcase> defaultProjects() {
        DashboardConfig.ProjectShowcase one = new DashboardConfig.ProjectShowcase();
        one.setTitle("Project Showcase A");
        one.setSummary("Add the problem, architecture decisions, and measurable results delivered for this client.");
        one.setTech("Spring Boot, PostgreSQL, REST");
        one.setImageUrl("https://images.unsplash.com/photo-1498050108023-c5249f4df085?q=80&w=1200&auto=format&fit=crop");

        DashboardConfig.ProjectShowcase two = new DashboardConfig.ProjectShowcase();
        two.setTitle("Project Showcase B");
        two.setSummary("Describe how you reduced risk, improved quality, and shipped on schedule.");
        two.setTech("TypeScript, React, MongoDB");
        two.setImageUrl("https://images.unsplash.com/photo-1517430816045-df4b7de11d1d?q=80&w=1200&auto=format&fit=crop");

        DashboardConfig.ProjectShowcase three = new DashboardConfig.ProjectShowcase();
        three.setTitle("Project Showcase C");
        three.setSummary("Highlight collaboration model, constraints, and final business outcomes.");
        three.setTech("Java, Event-driven systems, CI/CD");
        three.setImageUrl("https://images.unsplash.com/photo-1551288049-bebda4e38f71?q=80&w=1200&auto=format&fit=crop");

        return List.of(one, two, three);
    }

    private List<DashboardConfig.PortfolioLink> defaultLinks() {
        DashboardConfig.PortfolioLink github = new DashboardConfig.PortfolioLink();
        github.setLabel("GitHub");
        github.setUrl("#");

        DashboardConfig.PortfolioLink linkedin = new DashboardConfig.PortfolioLink();
        linkedin.setLabel("LinkedIn");
        linkedin.setUrl("#");

        DashboardConfig.PortfolioLink website = new DashboardConfig.PortfolioLink();
        website.setLabel("Personal Site");
        website.setUrl("#");

        return List.of(github, linkedin, website);
    }

    private String cleanString(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }

    private String portfolioUrl(Long userId) {
        return Routes.fill(Routes.Page.FREELANCER_PORTFOLIO, "userId", userId);
    }

    private DashboardConfig.FreelancerPortfolio fromDto(FreelancerPortfolioDtos.PortfolioContent dto) {
        if (dto == null) {
            return null;
        }
        DashboardConfig.FreelancerPortfolio portfolio = new DashboardConfig.FreelancerPortfolio();
        portfolio.setTagline(dto.tagline());
        portfolio.setSummary(dto.summary());
        portfolio.setLocation(dto.location());
        portfolio.setAvailability(dto.availability());
        portfolio.setSkills(dto.skills() == null ? List.of() : dto.skills());

        List<DashboardConfig.ProjectShowcase> projects = dto.projects() == null ? List.of() : dto.projects().stream()
            .filter(Objects::nonNull)
            .map(project -> {
                DashboardConfig.ProjectShowcase mapped = new DashboardConfig.ProjectShowcase();
                mapped.setTitle(project.title());
                mapped.setSummary(project.summary());
                mapped.setTech(project.tech());
                mapped.setImageUrl(project.imageUrl());
                return mapped;
            })
            .collect(Collectors.toList());
        portfolio.setProjects(projects);

        List<DashboardConfig.PortfolioLink> links = dto.links() == null ? List.of() : dto.links().stream()
            .filter(Objects::nonNull)
            .map(link -> {
                DashboardConfig.PortfolioLink mapped = new DashboardConfig.PortfolioLink();
                mapped.setLabel(link.label());
                mapped.setUrl(link.url());
                return mapped;
            })
            .collect(Collectors.toList());
        portfolio.setLinks(links);
        return portfolio;
    }

    private FreelancerPortfolioDtos.PortfolioContent toDto(DashboardConfig.FreelancerPortfolio portfolio) {
        List<FreelancerPortfolioDtos.PortfolioProject> projects = portfolio.getProjects().stream()
            .filter(Objects::nonNull)
            .map(project -> new FreelancerPortfolioDtos.PortfolioProject(
                project.getTitle(),
                project.getSummary(),
                project.getTech(),
                project.getImageUrl()
            ))
            .toList();

        List<FreelancerPortfolioDtos.PortfolioLink> links = portfolio.getLinks().stream()
            .filter(Objects::nonNull)
            .map(link -> new FreelancerPortfolioDtos.PortfolioLink(link.getLabel(), link.getUrl()))
            .toList();

        return new FreelancerPortfolioDtos.PortfolioContent(
            portfolio.getTagline(),
            portfolio.getSummary(),
            portfolio.getLocation(),
            portfolio.getAvailability(),
            portfolio.getSkills(),
            projects,
            links
        );
    }

    private FreelancerPortfolioDtos.TalentProfileSummary toTalentSummary(
        TalentProfile talentProfile,
        DashboardConfig.FreelancerPortfolio portfolio
    ) {
        if (talentProfile == null) {
            return new FreelancerPortfolioDtos.TalentProfileSummary(
                "Independent freelancer available for new contracts",
                DEFAULT_HOURLY_RATE,
                portfolio.getSkills()
            );
        }
        return new FreelancerPortfolioDtos.TalentProfileSummary(
            cleanString(talentProfile.getHeadline(), "Independent freelancer available for new contracts"),
            talentProfile.getHourlyRate() == null ? DEFAULT_HOURLY_RATE : talentProfile.getHourlyRate(),
            talentProfile.getSkills().stream().sorted().toList()
        );
    }
}
