package com.jackpot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jackpot.dto.BetRequest;
import com.jackpot.dto.JackpotEvaluationResponse;
import com.jackpot.model.Jackpot;
import com.jackpot.model.JackpotContribution;
import com.jackpot.repository.ContributionRepository;
import com.jackpot.repository.JackpotRepository;
import com.jackpot.repository.RewardRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive integration tests using Testcontainers for Kafka and PostgreSQL.
 * These tests run with real container instances of Kafka and PostgreSQL.
 */
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JackpotApplicationTestcontainersIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JackpotRepository jackpotRepository;

    @Autowired
    private ContributionRepository contributionRepository;

    @Autowired
    private RewardRepository rewardRepository;

    @BeforeEach
    void setUp() {
        // Clean up test data
        rewardRepository.deleteAll();
        contributionRepository.deleteAll();

        // Reset jackpots to initial values
        jackpotRepository.findAll().forEach(jackpot -> {
            jackpot.setCurrentPoolValue(jackpot.getInitialPoolValue());
            jackpotRepository.save(jackpot);
        });
    }

    @Test
    @Order(1)
    @DisplayName("Should verify Testcontainers are running")
    void testContainersAreRunning() {
        assertTrue(postgresContainer.isRunning(), "PostgreSQL container should be running");
        assertTrue(kafkaContainer.isRunning(), "Kafka container should be running");

        String jdbcUrl = getPostgresJdbcUrl();
        String kafkaBootstrap = getKafkaBootstrapServers();

        assertNotNull(jdbcUrl);
        assertNotNull(kafkaBootstrap);
        assertTrue(jdbcUrl.contains("jdbc:postgresql"));
        assertTrue(kafkaBootstrap.contains("PLAINTEXT"));
    }

    @Test
    @Order(2)
    @DisplayName("Should create jackpots in PostgreSQL database")
    void testJackpotsCreatedInPostgres() {
        // Given - jackpots are created by data.sql
        List<Jackpot> jackpots = jackpotRepository.findAll();

        // Then
        assertFalse(jackpots.isEmpty(), "Jackpots should exist");
        assertTrue(jackpots.size() >= 3, "Should have at least 3 jackpots");

        Jackpot jackpot1 = jackpotRepository.findById(1L).orElse(null);
        assertNotNull(jackpot1);
        assertEquals("FIXED_CONTRIBUTION", jackpot1.getContributionType());
        assertEquals("FIXED_REWARD", jackpot1.getRewardType());
        assertEquals(new BigDecimal("1000.00"), jackpot1.getInitialPoolValue());
    }

    @Test
    @Order(3)
    @DisplayName("Should submit bet via REST API and process with Kafka")
    void testBetSubmissionWithKafka() throws Exception {
        // Given
        Jackpot jackpot = jackpotRepository.findById(1L).orElseThrow();
        BigDecimal initialPool = jackpot.getCurrentPoolValue();

        BetRequest betRequest = new BetRequest(
                10001L,
                1001L,
                1L,
                new BigDecimal("100.00")
        );

        // When - Submit bet via REST API
        mockMvc.perform(post("/api/bets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(betRequest)))
                .andExpect(status().isAccepted())
                .andExpect(content().string("Bet published successfully"));

        // Wait for Kafka consumer to process
        Thread.sleep(2000);

        // Then - Verify contribution was recorded
        List<JackpotContribution> contributions = contributionRepository.findByBetId(10001L);
        assertEquals(1, contributions.size(), "Contribution should be recorded");

        JackpotContribution contribution = contributions.get(0);
        assertEquals(10001L, contribution.getBetId());
        assertEquals(1001L, contribution.getUserId());
        assertEquals(1L, contribution.getJackpotId());
        assertEquals(new BigDecimal("100.00"), contribution.getStakeAmount());
        assertEquals(new BigDecimal("10.00"), contribution.getContributionAmount()); // 10% of 100

        // Verify pool increased
        jackpot = jackpotRepository.findById(1L).orElseThrow();
        assertEquals(
                initialPool.add(new BigDecimal("10.00")),
                jackpot.getCurrentPoolValue(),
                "Pool should increase by contribution amount"
        );
    }

    @Test
    @Order(4)
    @DisplayName("Should handle multiple concurrent bets with Kafka")
    void testMultipleConcurrentBetsWithKafka() throws Exception {
        // Given
        Jackpot jackpot = jackpotRepository.findById(2L).orElseThrow();
        BigDecimal initialPool = jackpot.getCurrentPoolValue();
        int betCount = 5;

        // When - Submit multiple bets
        for (int i = 0; i < betCount; i++) {
            BetRequest betRequest = new BetRequest(
                    20001L + i,
                    2001L,
                    2L,
                    new BigDecimal("50.00")
            );

            mockMvc.perform(post("/api/bets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(betRequest)))
                    .andExpect(status().isAccepted());
        }

        // Wait for Kafka to process all bets
        Thread.sleep(3000);

        // Then - Verify all contributions were processed
        List<JackpotContribution> allContributions = contributionRepository.findByJackpotId(2L);
        assertTrue(allContributions.size() >= betCount,
                "All " + betCount + " bets should be processed");

        // Verify pool increased
        jackpot = jackpotRepository.findById(2L).orElseThrow();
        assertTrue(
                jackpot.getCurrentPoolValue().compareTo(initialPool) > 0,
                "Pool should increase after contributions"
        );
    }

    @Test
    @Order(5)
    @DisplayName("Should evaluate jackpot and handle wins correctly")
    void testJackpotEvaluationWithPostgres() throws Exception {
        // Given - Submit a bet first
        BetRequest betRequest = new BetRequest(
                30001L,
                3001L,
                1L,
                new BigDecimal("75.00")
        );

        mockMvc.perform(post("/api/bets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(betRequest)));

        // Wait for processing
        Thread.sleep(2000);

        // Get pool value before evaluation
        Jackpot jackpot = jackpotRepository.findById(1L).orElseThrow();
        BigDecimal poolBeforeEval = jackpot.getCurrentPoolValue();
        BigDecimal initialPool = jackpot.getInitialPoolValue();

        // When - Evaluate jackpot
        MvcResult result = mockMvc.perform(get("/api/jackpots/30001/evaluate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.won").isBoolean())
                .andExpect(jsonPath("$.rewardAmount").isNumber())
                .andExpect(jsonPath("$.message").isString())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JackpotEvaluationResponse response = objectMapper.readValue(
                responseBody,
                JackpotEvaluationResponse.class
        );

        // Then - Verify response structure
        assertNotNull(response);
        assertNotNull(response.getMessage());

        if (response.isWon()) {
            // If won, verify jackpot was reset
            assertTrue(response.getRewardAmount().compareTo(BigDecimal.ZERO) > 0);
            assertTrue(response.getMessage().contains("Congratulations"));

            jackpot = jackpotRepository.findById(1L).orElseThrow();
            assertEquals(initialPool, jackpot.getCurrentPoolValue(),
                    "Jackpot should reset to initial value after win");
        } else {
            // If not won, pool should remain unchanged
            assertEquals(BigDecimal.ZERO, response.getRewardAmount());
            assertTrue(response.getMessage().contains("Better luck"));

            jackpot = jackpotRepository.findById(1L).orElseThrow();
            assertEquals(poolBeforeEval, jackpot.getCurrentPoolValue(),
                    "Pool should not change if not won");
        }
    }

    @Test
    @Order(6)
    @DisplayName("Should retrieve jackpot details from PostgreSQL")
    void testGetJackpotFromPostgres() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/jackpots/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.initialPoolValue").value(1000.00))
                .andExpect(jsonPath("$.currentPoolValue").exists())
                .andExpect(jsonPath("$.contributionType").value("FIXED_CONTRIBUTION"))
                .andExpect(jsonPath("$.rewardType").value("FIXED_REWARD"))
                .andExpect(jsonPath("$.contributionConfig").exists())
                .andExpect(jsonPath("$.rewardConfig").exists());
    }

    @Test
    @Order(7)
    @DisplayName("Should handle variable contribution strategy with PostgreSQL")
    void testVariableContributionStrategyWithPostgres() throws Exception {
        // Given - Variable jackpot (ID 2)
        Jackpot jackpot = jackpotRepository.findById(2L).orElseThrow();
        assertEquals("VARIABLE_CONTRIBUTION", jackpot.getContributionType());
        BigDecimal initialPool = jackpot.getCurrentPoolValue();

        // When - Submit bet
        BetRequest betRequest = new BetRequest(
                40001L,
                4001L,
                2L,
                new BigDecimal("100.00")
        );

        mockMvc.perform(post("/api/bets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(betRequest)));

        Thread.sleep(2000);

        // Then - Verify variable contribution logic
        List<JackpotContribution> contributions = contributionRepository.findByBetId(40001L);
        assertEquals(1, contributions.size());

        JackpotContribution contribution = contributions.get(0);
        assertNotNull(contribution.getContributionAmount());
        assertTrue(contribution.getContributionAmount().compareTo(BigDecimal.ZERO) >= 0);

        // Pool should increase
        jackpot = jackpotRepository.findById(2L).orElseThrow();
        assertTrue(jackpot.getCurrentPoolValue().compareTo(initialPool) >= 0);
    }

    @Test
    @Order(8)
    @DisplayName("Should handle database transactions correctly")
    void testTransactionalBehavior() throws Exception {
        // Given
        long betCount = contributionRepository.count();

        BetRequest betRequest = new BetRequest(
                50001L,
                5001L,
                1L,
                new BigDecimal("150.00")
        );

        // When
        mockMvc.perform(post("/api/bets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(betRequest)));

        Thread.sleep(2000);

        // Then - Transaction should be committed
        long newCount = contributionRepository.count();
        assertEquals(betCount + 1, newCount, "Contribution should be persisted");

        // Verify data integrity
        List<JackpotContribution> contributions = contributionRepository.findByBetId(50001L);
        JackpotContribution contribution = contributions.get(0);

        Jackpot jackpot = jackpotRepository.findById(contribution.getJackpotId()).orElseThrow();
        assertNotNull(jackpot);
        assertEquals(contribution.getJackpotId(), jackpot.getId());
    }

    @Test
    @Order(9)
    @DisplayName("Should handle error scenarios gracefully")
    void testErrorHandling() throws Exception {
        // Given - Invalid jackpot ID
        BetRequest betRequest = new BetRequest(
                60001L,
                6001L,
                999L,  // Non-existent jackpot
                new BigDecimal("100.00")
        );

        // When & Then
        mockMvc.perform(post("/api/bets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(betRequest)))
                .andExpect(status().is5xxServerError());

        // Verify no contribution was created
        List<JackpotContribution> contributions = contributionRepository.findByBetId(60001L);
        assertTrue(contributions.isEmpty(), "No contribution should be created for invalid jackpot");
    }

    @Test
    @Order(10)
    @DisplayName("Should persist data across multiple operations")
    void testDataPersistenceInPostgres() throws Exception {
        // Submit multiple bets and verify persistence
        for (int i = 0; i < 3; i++) {
            BetRequest betRequest = new BetRequest(
                    70001L + i,
                    7001L,
                    3L,
                    new BigDecimal("25.00")
            );

            mockMvc.perform(post("/api/bets")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(betRequest)));
        }

        Thread.sleep(3000);

        // Verify all contributions are persisted
        List<JackpotContribution> contributions = contributionRepository.findByJackpotId(3L);
        assertTrue(contributions.size() >= 3, "All contributions should be persisted in PostgreSQL");

        // Verify data integrity
        for (JackpotContribution contrib : contributions) {
            assertNotNull(contrib.getId());
            assertNotNull(contrib.getCreatedAt());
            assertTrue(contrib.getContributionAmount().compareTo(BigDecimal.ZERO) > 0);
        }
    }
}
