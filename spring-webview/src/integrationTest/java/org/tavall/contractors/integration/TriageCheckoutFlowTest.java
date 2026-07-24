package org.tavall.contractors.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.tavall.couriers.api.web.endpoints.Routes;
import org.tavall.contractors.api.dto.CartAddonPayload;
import org.tavall.contractors.api.dto.IntakeStatePayload;
import org.tavall.contractors.api.dto.WizardSessionState;
import org.tavall.contractors.domain.jpa.CheckoutRecord;
import org.tavall.contractors.repo.jpa.CheckoutRecordRepository;
import org.tavall.contractors.repo.mongo.ProjectScopeRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class TriageCheckoutFlowTest {

    @Container
    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine");

    @Container
    private static final MongoDBContainer MONGODB = new MongoDBContainer("mongo:7");

    @DynamicPropertySource
    static void registerDataStores(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> POSTGRES.getJdbcUrl() + "?currentSchema=contractors");
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.mongodb.uri", MONGODB::getReplicaSetUrl);
        registry.add("spring.mongodb.database", () -> "tavall_contractors");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CheckoutRecordRepository checkoutRecordRepository;

    @Autowired
    private ProjectScopeRepository projectScopeRepository;

    @Test
    void triageCheckoutFlowPersistsRecordsInPostgresAndMongo() throws Exception {
        String sessionKey = "it-" + UUID.randomUUID();
        WizardSessionState state = new WizardSessionState();
        state.setSessionKey(sessionKey);
        state.setCurrentIntake(sampleScope("web", "Landing page rebuild", "3000-10000"));
        state.setCart(new ArrayList<>());
        state.setAddons(new ArrayList<>());
        state.setUpdatedAt(Instant.now());

        mockMvc.perform(put(Routes.fill(Routes.Api.Intake.STATE, "sessionKey", sessionKey))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(state)))
            .andExpect(status().isOk());

        mockMvc.perform(post(Routes.fill(Routes.Api.Intake.CART_ITEMS, "sessionKey", sessionKey))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    java.util.Map.of("scope", sampleScope("mobile", "iOS MVP", "1000-3000"))
                )))
            .andExpect(status().isOk());

        List<CartAddonPayload> addons = List.of(
            addon("priority_onboarding", "Priority Onboarding", "24 hour kickoff", new BigDecimal("299.00")),
            addon("qa_automation", "QA Automation Pack", "Regression suite setup", new BigDecimal("599.00"))
        );
        mockMvc.perform(put(Routes.fill(Routes.Api.Intake.ADDONS, "sessionKey", sessionKey))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(java.util.Map.of("addons", addons))))
            .andExpect(status().isOk());

        String checkoutBody = mockMvc.perform(post(Routes.Api.Intake.CHECKOUT)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"sessionKey\":\"" + sessionKey + "\"}"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        JsonNode checkoutJson = objectMapper.readTree(checkoutBody);
        long checkoutId = checkoutJson.get("checkoutId").asLong();

        CheckoutRecord checkoutRecord = checkoutRecordRepository.findById(checkoutId).orElseThrow();
        assertThat(checkoutRecord.getProjectCount()).isEqualTo(2);
        assertThat(checkoutRecord.getAddonCount()).isEqualTo(2);
        assertThat(checkoutRecord.getTotalBudget()).isEqualByComparingTo("8500.00");
        assertThat(checkoutRecord.getAddonTotal()).isEqualByComparingTo("898.00");
        assertThat(checkoutRecord.getGrandTotal()).isEqualByComparingTo("9398.00");

        var mongoDocs = projectScopeRepository.findByCheckoutId(checkoutId);
        assertThat(mongoDocs).hasSize(2);
        assertThat(mongoDocs).allMatch(doc -> doc.getClientId() != null);
    }

    private static IntakeStatePayload sampleScope(String domain, String domainType, String budget) {
        IntakeStatePayload payload = new IntakeStatePayload();
        payload.setDomain(domain);
        payload.setDomainType(domainType);
        payload.setTechLevel("dev");
        payload.setBudget(budget);
        payload.setDeadline("normal");
        payload.setContractType("fixed");
        payload.setDocsType(List.of("spec"));
        return payload;
    }

    private static CartAddonPayload addon(String code, String label, String description, BigDecimal price) {
        CartAddonPayload payload = new CartAddonPayload();
        payload.setCode(code);
        payload.setLabel(label);
        payload.setDescription(description);
        payload.setPrice(price);
        return payload;
    }
}
