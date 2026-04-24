package org.tavall.contractors.domain.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Document(collection = "dashboard_configs")
public class DashboardConfig {

    @Id
    private String id;

    private Long userId;

    private Map<String, Object> cssOverrides = new HashMap<>();

    private FreelancerPortfolio portfolio = new FreelancerPortfolio();

    private Instant updatedAt = Instant.now();

    public String getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Map<String, Object> getCssOverrides() {
        return cssOverrides;
    }

    public void setCssOverrides(Map<String, Object> cssOverrides) {
        this.cssOverrides = cssOverrides;
    }

    public FreelancerPortfolio getPortfolio() {
        return portfolio;
    }

    public void setPortfolio(FreelancerPortfolio portfolio) {
        this.portfolio = portfolio == null ? new FreelancerPortfolio() : portfolio;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static class FreelancerPortfolio {

        private String tagline;
        private String summary;
        private String location;
        private String availability;
        private List<String> skills = new ArrayList<>();
        private List<ProjectShowcase> projects = new ArrayList<>();
        private List<PortfolioLink> links = new ArrayList<>();

        public String getTagline() {
            return tagline;
        }

        public void setTagline(String tagline) {
            this.tagline = tagline;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getAvailability() {
            return availability;
        }

        public void setAvailability(String availability) {
            this.availability = availability;
        }

        public List<String> getSkills() {
            return skills;
        }

        public void setSkills(List<String> skills) {
            this.skills = skills == null ? new ArrayList<>() : skills;
        }

        public List<ProjectShowcase> getProjects() {
            return projects;
        }

        public void setProjects(List<ProjectShowcase> projects) {
            this.projects = projects == null ? new ArrayList<>() : projects;
        }

        public List<PortfolioLink> getLinks() {
            return links;
        }

        public void setLinks(List<PortfolioLink> links) {
            this.links = links == null ? new ArrayList<>() : links;
        }
    }

    public static class ProjectShowcase {

        private String title;
        private String summary;
        private String tech;
        private String imageUrl;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public String getTech() {
            return tech;
        }

        public void setTech(String tech) {
            this.tech = tech;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }
    }

    public static class PortfolioLink {

        private String label;
        private String url;

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}