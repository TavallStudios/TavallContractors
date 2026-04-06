# Tavall Migration: Spring Boot + Thymeleaf + TypeScript (Fat Server)

This implementation migrates the SPA to a server-first architecture:
- All compute/business rules run in Java services.
- TypeScript is a typed DOM + fetch layer only.
- PostgreSQL stores auth/users/roles (JPA).
- MongoDB stores dynamic scopes/projects/dashboard configs (MongoRepository).

## 1) Build + Runtime

### `spring-webview/pom.xml`
```xml
<dependencies>
  <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-web</artifactId></dependency>
  <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-thymeleaf</artifactId></dependency>
  <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-validation</artifactId></dependency>
  <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-data-jpa</artifactId></dependency>
  <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-data-mongodb</artifactId></dependency>
  <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-security</artifactId></dependency>
  <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-oauth2-client</artifactId></dependency>
  <dependency><groupId>org.postgresql</groupId><artifactId>postgresql</artifactId><scope>runtime</scope></dependency>
</dependencies>
```

### `spring-webview/src/main/resources/application.yml`
```yaml
spring:
  datasource:
    url: ${PG_URL:jdbc:postgresql://localhost:5432/tavall}
    username: ${PG_USER:tavall}
    password: ${PG_PASS:tavall}
  jpa:
    hibernate:
      ddl-auto: update
  data:
    mongodb:
      uri: ${MONGO_URI:mongodb://localhost:27017/tavall}
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope: openid,profile,email
          github:
            client-id: ${GITHUB_CLIENT_ID}
            client-secret: ${GITHUB_CLIENT_SECRET}
            scope: read:user,user:email
```

## 2) Polyglot Data Model

### PostgreSQL JPA

### `src/main/java/org/tavall/webview/domain/jpa/UserRole.java`
```java
package org.tavall.webview.domain.jpa;
public enum UserRole { CLIENT, TALENT, ADMIN }
```

### `src/main/java/org/tavall/webview/domain/jpa/UserAccount.java`
```java
package org.tavall.webview.domain.jpa;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name = "users")
public class UserAccount {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 320)
  private String email;

  @Column(nullable = false, length = 120)
  private String displayName;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private UserRole primaryRole = UserRole.CLIENT;

  @Column(nullable = false) private Instant createdAt;
  @Column(nullable = false) private Instant updatedAt;

  @PrePersist void onCreate(){ Instant now = Instant.now(); createdAt = now; updatedAt = now; }
  @PreUpdate void onUpdate(){ updatedAt = Instant.now(); }

  public Long getId(){ return id; }
  public String getEmail(){ return email; }
  public void setEmail(String email){ this.email = email; }
  public String getDisplayName(){ return displayName; }
  public void setDisplayName(String displayName){ this.displayName = displayName; }
  public UserRole getPrimaryRole(){ return primaryRole; }
  public void setPrimaryRole(UserRole primaryRole){ this.primaryRole = primaryRole; }
}
```

### `src/main/java/org/tavall/webview/domain/jpa/TalentProfile.java`
```java
package org.tavall.webview.domain.jpa;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity @Table(name = "talent_profiles")
public class TalentProfile {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private UserAccount user;

  @Column(length = 160) private String headline;
  @Column(nullable = false, precision = 12, scale = 2) private BigDecimal hourlyRate;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "talent_skills", joinColumns = @JoinColumn(name = "talent_profile_id"))
  @Column(name = "skill", nullable = false)
  private Set<String> skills = new HashSet<>();

  public UserAccount getUser(){ return user; }
  public String getHeadline(){ return headline; }
  public BigDecimal getHourlyRate(){ return hourlyRate; }
  public Set<String> getSkills(){ return skills; }
}
```
### `src/main/java/org/tavall/webview/domain/jpa/OAuthIdentity.java`
```java
package org.tavall.webview.domain.jpa;

import jakarta.persistence.*;

@Entity
@Table(name = "oauth_identities", uniqueConstraints = @UniqueConstraint(columnNames = {"provider", "providerUserId"}))
public class OAuthIdentity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private UserAccount user;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private AuthProvider provider;

  @Column(nullable = false, length = 255)
  private String providerUserId;

  @Column(length = 320)
  private String providerEmail;

  public enum AuthProvider { GOOGLE, GITHUB, MAGIC_LINK }
}
```

### MongoDB Documents

### `src/main/java/org/tavall/webview/domain/mongo/DashboardConfig.java`
```java
package org.tavall.webview.domain.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Document(collection = "dashboard_configs")
public class DashboardConfig {
  @Id private String id;
  private Long userId;
  private Map<String, Object> cssOverrides = new HashMap<>();
  private Instant updatedAt = Instant.now();
}
```

### `src/main/java/org/tavall/webview/domain/mongo/ProjectScopeDocument.java`
```java
package org.tavall.webview.domain.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Document(collection = "project_scopes")
public class ProjectScopeDocument {
  @Id private String id;
  private Long checkoutId;
  private Long clientId;
  private Long talentId;
  private String title;
  private String domain;
  private String status; // FUNDED, ACTIVE, REVIEW, APPROVED, COMPLETED, DISPUTED
  private BigDecimal totalBudget;
  private BigDecimal paidBudget;
  private BigDecimal escrowBalance;
  private String coverImage;
  private String generatedSpecMarkdown;
  private Map<String, Object> guidedAnswers = new HashMap<>();
  private Map<String, Object> intakeSnapshot = new HashMap<>();
  private List<Milestone> milestones = new ArrayList<>();

  public static class Milestone {
    public String id;
    public String title;
    public BigDecimal amount;
    public String status; // LOCKED, REVIEW, APPROVED, PAID

    public Milestone() {}
    public Milestone(String id, String title, BigDecimal amount, String status) {
      this.id = id; this.title = title; this.amount = amount; this.status = status;
    }
  }

  public String getId(){ return id; }
  public List<Milestone> getMilestones(){ return milestones; }
  public Long getClientId(){ return clientId; }
  public Long getTalentId(){ return talentId; }
  public String getStatus(){ return status; }
  public BigDecimal getTotalBudget(){ return totalBudget; }
  public BigDecimal getPaidBudget(){ return paidBudget; }
  public BigDecimal getEscrowBalance(){ return escrowBalance; }
  public String getGeneratedSpecMarkdown(){ return generatedSpecMarkdown; }
  public String getDomain(){ return domain; }
  public String getTitle(){ return title; }
  public void setCheckoutId(Long v){ checkoutId = v; }
  public void setClientId(Long v){ clientId = v; }
  public void setTalentId(Long v){ talentId = v; }
  public void setTitle(String v){ title = v; }
  public void setDomain(String v){ domain = v; }
  public void setStatus(String v){ status = v; }
  public void setTotalBudget(BigDecimal v){ totalBudget = v; }
  public void setPaidBudget(BigDecimal v){ paidBudget = v; }
  public void setEscrowBalance(BigDecimal v){ escrowBalance = v; }
  public void setGeneratedSpecMarkdown(String v){ generatedSpecMarkdown = v; }
  public void setGuidedAnswers(Map<String, Object> v){ guidedAnswers = v; }
  public void setIntakeSnapshot(Map<String, Object> v){ intakeSnapshot = v; }
  public void setMilestones(List<Milestone> v){ milestones = v; }
}
```

## 3) Repositories + Cache

### `src/main/java/org/tavall/webview/repo/jpa/*.java`
```java
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
  Optional<UserAccount> findByEmailIgnoreCase(String email);
}

public interface TalentProfileRepository extends JpaRepository<TalentProfile, Long> {
  @Query("""
    select tp from TalentProfile tp
    join fetch tp.user u
    where u.primaryRole = org.tavall.webview.domain.jpa.UserRole.TALENT
  """)
  List<TalentProfile> findAllTalentProfiles();
}

public interface OAuthIdentityRepository extends JpaRepository<OAuthIdentity, Long> {
  Optional<OAuthIdentity> findByProviderAndProviderUserId(OAuthIdentity.AuthProvider provider, String providerUserId);
}
```

### `src/main/java/org/tavall/webview/repo/mongo/ProjectScopeRepository.java`
```java
public interface ProjectScopeRepository extends MongoRepository<ProjectScopeDocument, String> {
  List<ProjectScopeDocument> findByStatusAndTalentIdIsNull(String status);
  List<ProjectScopeDocument> findByClientId(Long clientId);
  List<ProjectScopeDocument> findByTalentId(Long talentId);
  long countByStatus(String status);

  @Aggregation(pipeline = {
    "{ $match: { status: 'COMPLETED' } }",
    "{ $group: { _id: '$domain', totalProjects: { $sum: 1 }, coverImage: { $first: '$coverImage' } } }",
    "{ $project: { _id: 0, domain: '$_id', totalProjects: 1, coverImage: 1 } }"
  })
  List<PortfolioAggregate> aggregateCompletedByDomain();

  interface PortfolioAggregate { String getDomain(); Integer getTotalProjects(); String getCoverImage(); }
}
```

### `src/main/java/org/tavall/webview/cache/WizardStateCache.java`
```java
public interface WizardStateCache {
  void putState(String sessionKey, WizardSessionState state, Duration ttl);
  Optional<WizardSessionState> getState(String sessionKey);
  void evictState(String sessionKey);
  void putOtp(String email, String otp, Duration ttl);
  boolean verifyAndConsumeOtp(String email, String otp);
}
```

### `src/main/java/org/tavall/webview/cache/InMemoryWizardStateCache.java`
```java
@Component
public class InMemoryWizardStateCache implements WizardStateCache {
  private final Map<String, TimedValue<WizardSessionState>> stateStore = new ConcurrentHashMap<>();
  private final Map<String, TimedValue<String>> otpStore = new ConcurrentHashMap<>();
  private final ObjectMapper objectMapper;

  public InMemoryWizardStateCache(ObjectMapper objectMapper){ this.objectMapper = objectMapper; }

  public void putState(String key, WizardSessionState value, Duration ttl){ stateStore.put(key, new TimedValue<>(deepCopy(value), Instant.now().plus(ttl))); }
  public Optional<WizardSessionState> getState(String key){
    TimedValue<WizardSessionState> v = stateStore.get(key);
    if (v == null || v.expiresAt().isBefore(Instant.now())) { stateStore.remove(key); return Optional.empty(); }
    return Optional.of(deepCopy(v.value()));
  }
  public void evictState(String key){ stateStore.remove(key); }
  public void putOtp(String email, String otp, Duration ttl){ otpStore.put(email.toLowerCase(), new TimedValue<>(otp, Instant.now().plus(ttl))); }
  public boolean verifyAndConsumeOtp(String email, String otp){
    String key = email.toLowerCase(); TimedValue<String> v = otpStore.get(key);
    if (v == null || v.expiresAt().isBefore(Instant.now())) { otpStore.remove(key); return false; }
    boolean ok = v.value().equals(otp); if (ok) otpStore.remove(key); return ok;
  }

  @Scheduled(fixedDelay = 60000)
  void cleanupExpired(){ Instant now = Instant.now(); stateStore.entrySet().removeIf(e -> e.getValue().expiresAt().isBefore(now)); otpStore.entrySet().removeIf(e -> e.getValue().expiresAt().isBefore(now)); }

  private WizardSessionState deepCopy(WizardSessionState s){ return objectMapper.convertValue(s, WizardSessionState.class); }
  private record TimedValue<T>(T value, Instant expiresAt) {}
}
```
## 4) DTOs + Fat-Server Services

### `src/main/java/org/tavall/webview/api/dto/*.java`
```java
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
  // getters/setters omitted for brevity
}

public class WizardSessionState {
  private String sessionKey;
  private Long userId;
  private IntakeStatePayload currentIntake;
  private List<IntakeStatePayload> cart = new ArrayList<>();
  // getters/setters
}

public final class IntakeDtos {
  public record EvaluateRequest(IntakeStatePayload state) {}
  public record EvaluateResponse(IntakeStatePayload state, int completeness, String risk, List<String> badges) {}
  public record SpecRequest(IntakeStatePayload state) {}
  public record SpecResponse(String markdown) {}
  public record CheckoutRequest(String sessionKey) {}
  public record CheckoutResponse(Long checkoutId, int projectCount, BigDecimal totalBudget, String status) {}
}
```

### `src/main/java/org/tavall/webview/service/IntakeEvaluationService.java`
```java
@Service
public class IntakeEvaluationService {

  public EvaluationResult evaluate(IntakeStatePayload s) {
    int completeness = 0;
    if (has(s.getDomain())) completeness += 15;
    if (has(s.getDomainType())) completeness += 10;
    if (has(s.getTechLevel())) completeness += 10;
    if (has(s.getBudget())) completeness += 15;
    if (s.getDocsType() != null && !s.getDocsType().isEmpty()) completeness += 10;
    if (s.getGuidedAnswers() != null && !s.getGuidedAnswers().isEmpty()) completeness += 20;
    if (has(s.getContractType())) completeness += 10;
    if (has(s.getDeadline())) completeness += 10;
    completeness = Math.min(100, completeness);

    int riskScore = 35;
    String budget = safe(s.getBudget());
    if (budget.equals("no_budget") || budget.equals("no_budget_quote")) riskScore += 30;
    if (budget.equals("custom") && !has(s.getCustomBudget())) riskScore += 15;
    if (s.getDocsType() == null || s.getDocsType().isEmpty() || s.getDocsType().contains("none")) riskScore += 20;
    if ("dev".equals(s.getTechLevel()) || "very_tech".equals(s.getTechLevel())) riskScore -= 12;
    if ("non_tech".equals(s.getTechLevel())) riskScore += 8;
    if (s.getGuidedAnswers() == null || s.getGuidedAnswers().isEmpty()) riskScore += 20;
    riskScore = Math.max(0, Math.min(100, riskScore));

    String risk = riskScore >= 75 ? "Critical" : riskScore >= 45 ? "Med" : "Low";
    List<String> badges = new ArrayList<>();
    if (riskScore >= 80) badges.add("High Scope Ambiguity");
    if (riskScore <= 35) badges.add("Low-Risk Spec");
    if (completeness >= 85) badges.add("Execution Ready");

    s.setCompleteness(completeness);
    s.setRisk(risk);
    return new EvaluationResult(completeness, risk, badges, s);
  }

  private boolean has(String v){ return v != null && !v.isBlank(); }
  private String safe(String v){ return v == null ? "" : v; }
  public record EvaluationResult(int completeness, String risk, List<String> badges, IntakeStatePayload state) {}
}
```

### `src/main/java/org/tavall/webview/service/TechSpecGeneratorService.java`
```java
@Service
public class TechSpecGeneratorService {

  public String generateMarkdown(IntakeStatePayload state) {
    StringBuilder md = new StringBuilder();
    md.append("# Tavall Technical Specification\n\n");
    md.append("## Summary\n");
    md.append("- Domain: ").append(or(state.getDomain(), "Unspecified")).append("\n");
    md.append("- Type: ").append(or(state.getDomainType(), "Unspecified")).append("\n");
    md.append("- Contract: ").append(or(state.getContractType(), "Unspecified")).append("\n");
    md.append("- Budget: ").append(resolveBudget(state)).append("\n\n");

    md.append("## Functional Scope\n");
    appendGuidedAnswers(md, state.getGuidedAnswers(), 0);

    md.append("\n## Acceptance\n");
    md.append("- Milestones map to verifiable artifacts\n");
    md.append("- Change requests require funded change-order\n");
    md.append("- Review window: 72 hours per milestone\n");
    return md.toString();
  }

  private void appendGuidedAnswers(StringBuilder md, Map<String, Object> guidedAnswers, int depth) {
    if (guidedAnswers == null || guidedAnswers.isEmpty()) { md.append("- No guided answers supplied\n"); return; }
    for (Map.Entry<String, Object> entry : guidedAnswers.entrySet()) {
      String indent = "  ".repeat(Math.max(0, depth));
      md.append(indent).append("- ").append(entry.getKey().replace("_", " ")).append(": ");
      Object v = entry.getValue();
      if (v instanceof List<?> list) {
        md.append(list.stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(", "))).append("\n");
      } else if (v instanceof Map<?, ?> nested) {
        md.append("\n");
        @SuppressWarnings("unchecked") Map<String, Object> cast = (Map<String, Object>) nested;
        appendGuidedAnswers(md, cast, depth + 1);
      } else {
        md.append(String.valueOf(v)).append("\n");
      }
    }
  }

  private String resolveBudget(IntakeStatePayload s){
    if ("custom".equals(s.getBudget()) && s.getCustomBudget() != null) return s.getCustomBudget();
    return or(s.getBudget(), "Unspecified");
  }
  private String or(String v, String fb){ return (v == null || v.isBlank()) ? fb : v; }
}
```

### `src/main/java/org/tavall/webview/service/CheckoutService.java`
```java
@Service
public class CheckoutService {
  private final WizardStateCache cache;
  private final UserAccountRepository userRepo;
  private final CheckoutRecordRepository checkoutRepo;
  private final ProjectScopeRepository scopeRepo;
  private final IntakeEvaluationService eval;
  private final TechSpecGeneratorService spec;

  public CheckoutService(WizardStateCache cache, UserAccountRepository userRepo, CheckoutRecordRepository checkoutRepo,
                         ProjectScopeRepository scopeRepo, IntakeEvaluationService eval, TechSpecGeneratorService spec) {
    this.cache = cache; this.userRepo = userRepo; this.checkoutRepo = checkoutRepo; this.scopeRepo = scopeRepo;
    this.eval = eval; this.spec = spec;
  }

  @Transactional
  public IntakeDtos.CheckoutResponse checkout(Long userId, IntakeDtos.CheckoutRequest request) {
    WizardSessionState cached = cache.getState(request.sessionKey())
      .orElseThrow(() -> new IllegalArgumentException("No cached wizard state"));
    UserAccount user = userRepo.findById(userId).orElseThrow();

    List<IntakeStatePayload> scopes = new ArrayList<>();
    if (cached.getCart() != null) scopes.addAll(cached.getCart());
    if (cached.getCurrentIntake() != null) scopes.add(cached.getCurrentIntake());
    if (scopes.isEmpty()) throw new IllegalStateException("Checkout blocked: empty cart");

    BigDecimal total = scopes.stream().map(this::budgetValue).reduce(BigDecimal.ZERO, BigDecimal::add);

    CheckoutRecord rec = new CheckoutRecord();
    rec.setUser(user); rec.setSessionKey(request.sessionKey()); rec.setProjectCount(scopes.size()); rec.setTotalBudget(total);
    rec = checkoutRepo.save(rec);

    List<String> insertedIds = new ArrayList<>();
    try {
      for (IntakeStatePayload s : scopes) {
        eval.evaluate(s);
        ProjectScopeDocument doc = new ProjectScopeDocument();
        doc.setCheckoutId(rec.getId());
        doc.setClientId(user.getId());
        doc.setTitle(s.getDomainType() != null ? s.getDomainType() : "Scoped Project");
        doc.setDomain(s.getDomain());
        doc.setStatus("FUNDED");
        BigDecimal budget = budgetValue(s);
        doc.setTotalBudget(budget); doc.setPaidBudget(BigDecimal.ZERO); doc.setEscrowBalance(budget);
        doc.setGeneratedSpecMarkdown(spec.generateMarkdown(s));
        doc.setGuidedAnswers(s.getGuidedAnswers());
        doc.setMilestones(defaultMilestones(budget));
        ProjectScopeDocument inserted = scopeRepo.save(doc);
        insertedIds.add(inserted.getId());
      }
    } catch (RuntimeException ex) {
      if (!insertedIds.isEmpty()) scopeRepo.deleteAllById(insertedIds);
      throw ex;
    }

    cache.evictState(request.sessionKey());
    return new IntakeDtos.CheckoutResponse(rec.getId(), scopes.size(), total, "FUNDED");
  }

  private List<ProjectScopeDocument.Milestone> defaultMilestones(BigDecimal total){
    BigDecimal m1 = total.multiply(BigDecimal.valueOf(0.25)).setScale(2, RoundingMode.HALF_UP);
    BigDecimal m2 = total.multiply(BigDecimal.valueOf(0.5)).setScale(2, RoundingMode.HALF_UP);
    BigDecimal m3 = total.subtract(m1).subtract(m2);
    return List.of(new ProjectScopeDocument.Milestone("m1", "Scope + Architecture", m1, "LOCKED"),
                   new ProjectScopeDocument.Milestone("m2", "Implementation", m2, "LOCKED"),
                   new ProjectScopeDocument.Milestone("m3", "QA + Handover", m3, "LOCKED"));
  }

  private BigDecimal budgetValue(IntakeStatePayload s){
    return switch (String.valueOf(s.getBudget())) {
      case "1000-3000" -> BigDecimal.valueOf(2000);
      case "3000-10000" -> BigDecimal.valueOf(6500);
      case "10000+" -> BigDecimal.valueOf(15000);
      case "custom" -> parseCurrency(s.getCustomBudget());
      default -> BigDecimal.ZERO;
    };
  }

  private BigDecimal parseCurrency(String raw){
    if (raw == null || raw.isBlank()) return BigDecimal.ZERO;
    String n = raw.replaceAll("[^0-9.]", "");
    return n.isBlank() ? BigDecimal.ZERO : new BigDecimal(n).setScale(2, RoundingMode.HALF_UP);
  }
}
```
## 5) Auth + REST Controllers

### `src/main/java/org/tavall/webview/security/SecurityConfig.java`
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http, OAuth2LinkingUserService oauthService) throws Exception {
    http
      .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
      .authorizeHttpRequests(auth -> auth
        .requestMatchers("/", "/hire-direct", "/portfolios", "/freelancer", "/css/**", "/js/**").permitAll()
        .requestMatchers("/api/auth/**").permitAll()
        .requestMatchers(HttpMethod.GET, "/api/hire-direct/**", "/api/portfolios/**", "/api/freelancer/**").permitAll()
        .requestMatchers("/api/admin/**").hasRole("ADMIN")
        .anyRequest().authenticated())
      .oauth2Login(oauth -> oauth.userInfoEndpoint(ui -> ui.userService(oauthService)))
      .formLogin(form -> form.disable());
    return http.build();
  }
}
```

### `src/main/java/org/tavall/webview/security/OAuth2LinkingUserService.java`
```java
@Service
public class OAuth2LinkingUserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
  private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
  private final UserAccountRepository userRepo;
  private final OAuthIdentityRepository identityRepo;

  public OAuth2LinkingUserService(UserAccountRepository userRepo, OAuthIdentityRepository identityRepo){
    this.userRepo = userRepo; this.identityRepo = identityRepo;
  }

  @Override
  public OAuth2User loadUser(OAuth2UserRequest req) throws OAuth2AuthenticationException {
    OAuth2User oauthUser = delegate.loadUser(req);
    Map<String, Object> attrs = oauthUser.getAttributes();

    String reg = req.getClientRegistration().getRegistrationId().toUpperCase(Locale.ROOT);
    OAuthIdentity.AuthProvider provider = OAuthIdentity.AuthProvider.valueOf(reg);

    String providerUserId = String.valueOf(attrs.getOrDefault("sub", attrs.getOrDefault("id", "")));
    String email = String.valueOf(attrs.getOrDefault("email", ""));
    if (email.isBlank() && "GITHUB".equals(reg)) {
      email = String.valueOf(attrs.getOrDefault("login", "github-user")) + "@users.noreply.github.com";
    }
    String normalizedEmail = email.toLowerCase(Locale.ROOT);

    UserAccount user = userRepo.findByEmailIgnoreCase(normalizedEmail).orElseGet(() -> {
      UserAccount created = new UserAccount();
      created.setEmail(normalizedEmail);
      created.setDisplayName(String.valueOf(attrs.getOrDefault("name", normalizedEmail)));
      created.setPrimaryRole(UserRole.CLIENT);
      return userRepo.save(created);
    });

    OAuthIdentity identity = identityRepo.findByProviderAndProviderUserId(provider, providerUserId).orElseGet(OAuthIdentity::new);
    identity.setProvider(provider); identity.setProviderUserId(providerUserId); identity.setProviderEmail(normalizedEmail); identity.setUser(user);
    identityRepo.save(identity);

    Set<GrantedAuthority> authorities = Set.of(new SimpleGrantedAuthority("ROLE_" + user.getPrimaryRole().name()));
    String nameAttr = attrs.containsKey("email") ? "email" : attrs.containsKey("sub") ? "sub" : "id";
    return new DefaultOAuth2User(authorities, attrs, nameAttr);
  }
}
```

### `src/main/java/org/tavall/webview/service/MagicLinkService.java`
```java
@Service
public class MagicLinkService {
  private static final Logger log = LoggerFactory.getLogger(MagicLinkService.class);
  private final WizardStateCache cache;
  private final UserAccountRepository userRepo;

  public MagicLinkService(WizardStateCache cache, UserAccountRepository userRepo){ this.cache = cache; this.userRepo = userRepo; }

  public void requestOtp(String email){
    String normalized = email.trim().toLowerCase(Locale.ROOT);
    String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 999999));
    cache.putOtp(normalized, otp, Duration.ofMinutes(10));
    log.info("[MAGIC-LINK] Simulated OTP {} to {}", otp, normalized);
  }

  public UserAccount verifyOtp(String email, String otp){
    String normalized = email.trim().toLowerCase(Locale.ROOT);
    if (!cache.verifyAndConsumeOtp(normalized, otp)) throw new IllegalArgumentException("Invalid or expired OTP");
    return userRepo.findByEmailIgnoreCase(normalized).orElseGet(() -> {
      UserAccount user = new UserAccount();
      user.setEmail(normalized);
      user.setDisplayName(normalized.split("@")[0]);
      user.setPrimaryRole(UserRole.CLIENT);
      return userRepo.save(user);
    });
  }
}
```

### `src/main/java/org/tavall/webview/api/AuthController.java`
```java
@RestController
@RequestMapping("/api/auth/magic")
public class AuthController {
  private final MagicLinkService magic;
  public AuthController(MagicLinkService magic){ this.magic = magic; }

  @PostMapping("/request")
  public Map<String, String> request(@RequestBody MagicRequest req){ magic.requestOtp(req.email()); return Map.of("status", "OTP_SENT"); }

  @PostMapping("/verify")
  public Map<String, Object> verify(@RequestBody MagicVerifyRequest req){
    UserAccount user = magic.verifyOtp(req.email(), req.otp());
    var auth = new UsernamePasswordAuthenticationToken(user.getId(), null, List.of(new SimpleGrantedAuthority("ROLE_" + user.getPrimaryRole().name())));
    SecurityContextHolder.getContext().setAuthentication(auth);
    return Map.of("status", "AUTHENTICATED", "userId", user.getId(), "email", user.getEmail(), "role", user.getPrimaryRole().name());
  }

  public record MagicRequest(String email) {}
  public record MagicVerifyRequest(String email, String otp) {}
}
```

### `src/main/java/org/tavall/webview/service/MarketplaceReadService.java`
```java
@Service
public class MarketplaceReadService {
  private final TalentProfileRepository talentRepo;
  private final UserAccountRepository userRepo;
  private final ProjectScopeRepository scopeRepo;

  public MarketplaceReadService(TalentProfileRepository talentRepo, UserAccountRepository userRepo, ProjectScopeRepository scopeRepo){
    this.talentRepo = talentRepo; this.userRepo = userRepo; this.scopeRepo = scopeRepo;
  }

  public List<TalentCard> hireDirect(){
    return talentRepo.findAllTalentProfiles().stream()
      .map(tp -> new TalentCard(tp.getUser().getId(), tp.getUser().getDisplayName(), tp.getHeadline(), tp.getHourlyRate(), tp.getSkills().stream().sorted().toList()))
      .toList();
  }

  public List<PortfolioCard> portfolios(){
    return scopeRepo.aggregateCompletedByDomain().stream().map(p -> new PortfolioCard(p.getDomain(), p.getTotalProjects(), p.getCoverImage())).toList();
  }

  public List<ProjectScopeDocument> fundedJobs(){ return scopeRepo.findByStatusAndTalentIdIsNull("FUNDED"); }

  public ClientDashboard clientDashboard(Long userId){
    List<ProjectScopeDocument> projects = scopeRepo.findByClientId(userId);
    BigDecimal total = projects.stream().map(ProjectScopeDocument::getTotalBudget).reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal paid = projects.stream().map(ProjectScopeDocument::getPaidBudget).reduce(BigDecimal.ZERO, BigDecimal::add);
    ProjectScopeDocument.Milestone next = projects.stream().flatMap(p -> p.getMilestones().stream()).filter(m -> !"PAID".equals(m.status)).findFirst().orElse(null);
    return new ClientDashboard(projects, paid, total, next);
  }

  public FreelancerDashboard freelancerDashboard(Long userId){
    List<ProjectScopeDocument> projects = scopeRepo.findByTalentId(userId);
    BigDecimal available = projects.stream().flatMap(p -> p.getMilestones().stream())
      .filter(m -> "REVIEW".equals(m.status) || "APPROVED".equals(m.status))
      .map(m -> m.amount).reduce(BigDecimal.ZERO, BigDecimal::add);
    return new FreelancerDashboard(projects, available);
  }

  public AdminStats adminStats(){
    long users = userRepo.count();
    BigDecimal activeEscrow = scopeRepo.findAll().stream()
      .filter(p -> Set.of("FUNDED", "ACTIVE", "REVIEW").contains(p.getStatus()))
      .map(ProjectScopeDocument::getEscrowBalance).reduce(BigDecimal.ZERO, BigDecimal::add);
    long disputes = scopeRepo.countByStatus("DISPUTED");
    return new AdminStats(users, activeEscrow, disputes);
  }

  public record TalentCard(Long userId, String name, String headline, BigDecimal hourlyRate, List<String> skills) {}
  public record PortfolioCard(String domain, Integer totalProjects, String coverImage) {}
  public record ClientDashboard(List<ProjectScopeDocument> projects, BigDecimal paidBudget, BigDecimal totalBudget, ProjectScopeDocument.Milestone nextActiveMilestone) {}
  public record FreelancerDashboard(List<ProjectScopeDocument> projects, BigDecimal availablePayout) {}
  public record AdminStats(long users, BigDecimal activeEscrow, long disputedProjects) {}
}
```
### `src/main/java/org/tavall/webview/api/IntakeController.java`
```java
@RestController
@RequestMapping("/api/intake")
public class IntakeController {
  private final WizardStateCache cache;
  private final IntakeEvaluationService eval;
  private final TechSpecGeneratorService spec;
  private final CheckoutService checkout;

  public IntakeController(WizardStateCache cache, IntakeEvaluationService eval, TechSpecGeneratorService spec, CheckoutService checkout){
    this.cache = cache; this.eval = eval; this.spec = spec; this.checkout = checkout;
  }

  @PutMapping("/state/{sessionKey}")
  public WizardSessionState putState(@PathVariable String sessionKey, @RequestBody WizardSessionState state, Authentication auth){
    state.setSessionKey(sessionKey); state.setUserId(resolveUserId(auth));
    cache.putState(sessionKey, state, Duration.ofHours(6));
    return state;
  }

  @GetMapping("/state/{sessionKey}")
  public WizardSessionState getState(@PathVariable String sessionKey){
    return cache.getState(sessionKey).orElseThrow(() -> new IllegalArgumentException("No state found"));
  }

  @PostMapping("/evaluate")
  public IntakeDtos.EvaluateResponse evaluate(@RequestBody IntakeDtos.EvaluateRequest req){
    var r = eval.evaluate(req.state());
    return new IntakeDtos.EvaluateResponse(r.state(), r.completeness(), r.risk(), r.badges());
  }

  @PostMapping("/spec")
  public IntakeDtos.SpecResponse generateSpec(@RequestBody IntakeDtos.SpecRequest req){
    return new IntakeDtos.SpecResponse(spec.generateMarkdown(req.state()));
  }

  @PostMapping("/checkout")
  public IntakeDtos.CheckoutResponse checkout(@RequestBody IntakeDtos.CheckoutRequest req, Authentication auth){
    return checkout.checkout(resolveUserId(auth), req);
  }

  private Long resolveUserId(Authentication auth){
    if (auth == null || auth.getPrincipal() == null) throw new IllegalStateException("Authenticated user required");
    Object p = auth.getPrincipal();
    if (p instanceof Long v) return v;
    if (p instanceof String v && v.matches("\\d+")) return Long.valueOf(v);
    throw new IllegalStateException("Unable to resolve user id");
  }
}
```

### `src/main/java/org/tavall/webview/api/MarketplaceController.java`
```java
@RestController
@RequestMapping("/api")
public class MarketplaceController {
  private final MarketplaceReadService read;
  public MarketplaceController(MarketplaceReadService read){ this.read = read; }

  @GetMapping("/hire-direct/talent") public Object hireDirect(){ return read.hireDirect(); }
  @GetMapping("/portfolios") public Object portfolios(){ return read.portfolios(); }
  @GetMapping("/freelancer/jobs") public Object freelancerJobs(){ return read.fundedJobs(); }
  @GetMapping("/dashboard/client") public Object clientDashboard(Authentication auth){ return read.clientDashboard(resolveUserId(auth)); }
  @GetMapping("/dashboard/freelancer") public Object freelancerDashboard(Authentication auth){ return read.freelancerDashboard(resolveUserId(auth)); }
  @GetMapping("/admin/stats") public Object adminStats(){ return read.adminStats(); }

  private Long resolveUserId(Authentication auth){ Object p = auth.getPrincipal(); return p instanceof Long v ? v : Long.valueOf(String.valueOf(p)); }
}
```

## 6) Thymeleaf + TypeScript Integration

### `src/main/resources/templates/index.html`
```html
<body th:attr="data-page=${page}">
  <main>
    <section th:if="${page == 'hire-direct'}"><div id="hire-direct-list"></div></section>
    <section th:if="${page == 'portfolios'}"><div id="portfolio-grid"></div></section>
    <section th:if="${page == 'freelancer'}"><div id="freelancer-jobs"></div></section>
    <section th:if="${page == 'client-dashboard'}"><div id="client-dashboard-root"></div></section>
    <section th:if="${page == 'freelancer-dashboard'}"><div id="freelancer-dashboard-root"></div></section>
  </main>

  <div id="intake-modal-root" hidden>
    <form id="intake-form">
      <input name="domain" />
      <input name="domainType" />
      <input name="techLevel" />
      <input name="budget" />
      <input name="customBudget" />
      <input name="deadline" />
      <input name="contractType" />
      <label><input type="checkbox" name="docsType" value="none" /> None</label>
      <label><input type="checkbox" name="docsType" value="design" /> Figma</label>
      <label><input type="checkbox" name="docsType" value="spec" /> Spec</label>
      <label><input type="checkbox" name="docsType" value="repo" /> Repo</label>
      <div id="completeness-badge"></div>
      <div id="risk-badge"></div>
      <pre id="generated-spec"></pre>
      <button type="button" id="generate-spec">Generate Spec</button>
      <button type="button" id="checkout">Checkout</button>
    </form>
  </div>

  <script type="module" th:src="@{/js/main.js}"></script>
</body>
```

### `src/main/resources/static/ts/types.ts`
```ts
export interface IntakeState {
  domain: string;
  domainType: string;
  domainSubtype: string;
  language: string;
  techLevel: string;
  budget: string;
  customBudget: string;
  deadline: string;
  docsType: string[];
  guidedAnswers: Record<string, unknown>;
  contractType: string;
  completeness?: number;
  risk?: "Low" | "Med" | "Critical";
}
export interface WizardSessionState { sessionKey: string; userId?: number; currentIntake: IntakeState; cart: IntakeState[]; }
```

### `src/main/resources/static/ts/api.ts`
```ts
export const IntakeApi = {
  putState: (sessionKey: string, state: unknown) => fetch(`/api/intake/state/${sessionKey}`, { method: "PUT", credentials: "include", headers: { "Content-Type": "application/json" }, body: JSON.stringify(state) }),
  getState: (sessionKey: string) => fetch(`/api/intake/state/${sessionKey}`, { credentials: "include" }).then(r => r.json()),
  evaluate: (state: unknown) => fetch("/api/intake/evaluate", { method: "POST", credentials: "include", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ state }) }).then(r => r.json()),
  generateSpec: (state: unknown) => fetch("/api/intake/spec", { method: "POST", credentials: "include", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ state }) }).then(r => r.json()),
  checkout: (sessionKey: string) => fetch("/api/intake/checkout", { method: "POST", credentials: "include", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ sessionKey }) }).then(r => r.json())
};
```

### `src/main/resources/static/ts/intake.ts`
```ts
import { IntakeApi } from "./api";

const debounce = <T extends (...args: never[]) => void>(fn: T, wait = 300): T => {
  let t: number | undefined;
  return ((...args: never[]) => { if (t) clearTimeout(t); t = window.setTimeout(() => fn(...args), wait); }) as T;
};

export async function wireIntake(): Promise<void> {
  const form = document.getElementById("intake-form") as HTMLFormElement | null;
  if (!form) return;

  const sessionKey = sessionStorage.getItem("tavall:intake:session") ?? crypto.randomUUID();
  sessionStorage.setItem("tavall:intake:session", sessionKey);

  let sessionState = await IntakeApi.getState(sessionKey).catch(() => ({ sessionKey, currentIntake: { domain: "", domainType: "", domainSubtype: "", language: "", techLevel: "", budget: "", customBudget: "", deadline: "", docsType: [], guidedAnswers: {}, contractType: "" }, cart: [] }));
  const saveDebounced = debounce(() => { void IntakeApi.putState(sessionKey, sessionState); });

  const readState = () => {
    const fd = new FormData(form);
    return {
      ...sessionState.currentIntake,
      domain: String(fd.get("domain") ?? ""),
      domainType: String(fd.get("domainType") ?? ""),
      techLevel: String(fd.get("techLevel") ?? ""),
      budget: String(fd.get("budget") ?? ""),
      customBudget: String(fd.get("customBudget") ?? ""),
      deadline: String(fd.get("deadline") ?? ""),
      contractType: String(fd.get("contractType") ?? ""),
      docsType: fd.getAll("docsType").map(String)
    };
  };

  form.addEventListener("change", async () => {
    sessionState.currentIntake = readState();
    const evalRes = await IntakeApi.evaluate(sessionState.currentIntake);
    sessionState.currentIntake = evalRes.state;
    (document.getElementById("completeness-badge") as HTMLElement | null)?.replaceChildren(`${evalRes.completeness}% complete` as unknown as Node);
    (document.getElementById("risk-badge") as HTMLElement | null)?.replaceChildren(`${evalRes.risk} risk` as unknown as Node);
    saveDebounced();
  });

  document.getElementById("generate-spec")?.addEventListener("click", async () => {
    const spec = await IntakeApi.generateSpec(sessionState.currentIntake);
    const out = document.getElementById("generated-spec");
    if (out) out.textContent = spec.markdown;
  });

  document.getElementById("checkout")?.addEventListener("click", async () => {
    const done = await IntakeApi.checkout(sessionKey);
    alert(`Checkout ${done.status}: ${done.checkoutId}`);
  });
}
```

### `src/main/resources/static/ts/main.ts`
```ts
import { wireIntake } from "./intake";
void wireIntake();
```

## 7) End-to-End Data Hydration Paths

- `/hire-direct`: `TalentProfileRepository.findAllTalentProfiles()` from Postgres.
- `/portfolios`: Mongo aggregation on `project_scopes` status `COMPLETED` grouped by domain.
- `/freelancer`: Mongo query for `status='FUNDED'` and `talentId == null`.
- Client dashboard: Mongo `clientId = currentUser.id`, progress computed in Java from `paidBudget / totalBudget`, next active milestone resolved server-side.
- Freelancer dashboard: Mongo `talentId = currentUser.id`, available payout summed in Java for milestones `REVIEW` or `APPROVED`.
- `/api/admin/stats`: count users from Postgres, sum active escrow from Mongo, count disputed projects from Mongo.

This is the requested dumb-client / fat-server architecture with strict Postgres-vs-Mongo domain separation and debounced cache-backed intake state orchestration.
