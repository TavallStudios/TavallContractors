package org.tavall.contractors.api.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IntakeStatePayload {

    private String domain;
    private String domainType;
    private String domainSubtype;
    private String language;
    private String techLevel;
    private String budget;
    private String customBudget;
    private String deadline;
    private List<String> docsType = new ArrayList<>();
    private Map<String, Object> guidedAnswers = new HashMap<>();
    private String contractType;
    private Integer completeness;
    private String risk;

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getDomainType() {
        return domainType;
    }

    public void setDomainType(String domainType) {
        this.domainType = domainType;
    }

    public String getDomainSubtype() {
        return domainSubtype;
    }

    public void setDomainSubtype(String domainSubtype) {
        this.domainSubtype = domainSubtype;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getTechLevel() {
        return techLevel;
    }

    public void setTechLevel(String techLevel) {
        this.techLevel = techLevel;
    }

    public String getBudget() {
        return budget;
    }

    public void setBudget(String budget) {
        this.budget = budget;
    }

    public String getCustomBudget() {
        return customBudget;
    }

    public void setCustomBudget(String customBudget) {
        this.customBudget = customBudget;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public List<String> getDocsType() {
        return docsType;
    }

    public void setDocsType(List<String> docsType) {
        this.docsType = docsType;
    }

    public Map<String, Object> getGuidedAnswers() {
        return guidedAnswers;
    }

    public void setGuidedAnswers(Map<String, Object> guidedAnswers) {
        this.guidedAnswers = guidedAnswers;
    }

    public String getContractType() {
        return contractType;
    }

    public void setContractType(String contractType) {
        this.contractType = contractType;
    }

    public Integer getCompleteness() {
        return completeness;
    }

    public void setCompleteness(Integer completeness) {
        this.completeness = completeness;
    }

    public String getRisk() {
        return risk;
    }

    public void setRisk(String risk) {
        this.risk = risk;
    }
}