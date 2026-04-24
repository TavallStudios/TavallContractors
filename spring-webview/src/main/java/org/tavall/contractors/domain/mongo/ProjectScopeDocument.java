package org.tavall.contractors.domain.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Document(collection = "project_scopes")
public class ProjectScopeDocument {

    @Id
    private String id;

    private Long checkoutId;
    private Long clientId;
    private Long talentId;

    private String title;
    private String domain;
    private String status;

    private BigDecimal totalBudget;
    private BigDecimal paidBudget;
    private BigDecimal escrowBalance;

    private String coverImage;
    private String generatedSpecMarkdown;

    private Map<String, Object> guidedAnswers = new HashMap<>();
    private Map<String, Object> intakeSnapshot = new HashMap<>();
    private List<Milestone> milestones = new ArrayList<>();

    private Instant createdAt = Instant.now();

    public static class Milestone {
        private String id;
        private String title;
        private BigDecimal amount;
        private String status;

        public Milestone() {
        }

        public Milestone(String id, String title, BigDecimal amount, String status) {
            this.id = id;
            this.title = title;
            this.amount = amount;
            this.status = status;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public String getStatus() {
            return status;
        }
    }

    public String getId() {
        return id;
    }

    public Long getCheckoutId() {
        return checkoutId;
    }

    public void setCheckoutId(Long checkoutId) {
        this.checkoutId = checkoutId;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Long getTalentId() {
        return talentId;
    }

    public void setTalentId(Long talentId) {
        this.talentId = talentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getTotalBudget() {
        return totalBudget;
    }

    public void setTotalBudget(BigDecimal totalBudget) {
        this.totalBudget = totalBudget;
    }

    public BigDecimal getPaidBudget() {
        return paidBudget;
    }

    public void setPaidBudget(BigDecimal paidBudget) {
        this.paidBudget = paidBudget;
    }

    public BigDecimal getEscrowBalance() {
        return escrowBalance;
    }

    public void setEscrowBalance(BigDecimal escrowBalance) {
        this.escrowBalance = escrowBalance;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public String getGeneratedSpecMarkdown() {
        return generatedSpecMarkdown;
    }

    public void setGeneratedSpecMarkdown(String generatedSpecMarkdown) {
        this.generatedSpecMarkdown = generatedSpecMarkdown;
    }

    public Map<String, Object> getGuidedAnswers() {
        return guidedAnswers;
    }

    public void setGuidedAnswers(Map<String, Object> guidedAnswers) {
        this.guidedAnswers = guidedAnswers;
    }

    public Map<String, Object> getIntakeSnapshot() {
        return intakeSnapshot;
    }

    public void setIntakeSnapshot(Map<String, Object> intakeSnapshot) {
        this.intakeSnapshot = intakeSnapshot;
    }

    public List<Milestone> getMilestones() {
        return milestones;
    }

    public void setMilestones(List<Milestone> milestones) {
        this.milestones = milestones;
    }
}