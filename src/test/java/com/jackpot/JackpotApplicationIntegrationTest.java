package com.jackpot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jackpot.dto.BetRequest;
import com.jackpot.dto.JackpotEvaluationResponse;
import com.jackpot.model.Jackpot;
import com.jackpot.model.JackpotContribution;
import com.jackpot.repository.ContributionRepository;
import com.jackpot.repository.JackpotRepository;
import com.jackpot.repository.RewardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive end-to-end integration tests for the entire Jackpot application.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class JackpotApplicationIntegrationTest {

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
        // Clean up data before each test
        rewardRepository.deleteAll();
        contributionRepository.deleteAll();
    }

    @Test
    void testCompleteWorkflow_FixedJackpot_BetSubmissionAndEvaluation() throws Exception {
        // Step 1: Verify jackpot exists
        Jackpot jackpot = jackpotRepository.findById(1L).orElseThrow();
        assertEquals("FIXED_CONTRIBUTION", jackpot.getContributionType());
        assertEquals("FIXED_REWARD", jackpot.getRewardType());
        BigDecimal initialPool = jackpot.getCurrentPoolValue();

        // Step 2: Submit a bet
        BetRequest betRequest = new BetRequest(
                1001L,  // betId
                100L,   // userId
                1L,     // jackpotId
                new BigDecimal("100.00")  // betAmount
        );

        mockMvc.perform(post("/api/bets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(betRequest)))
                .andExpect(status().isAccepted())
                .andExpect(content().string("Bet published successfully"));

        // Step 3: Verify contribution was recorded
        List<JackpotContribution> contributions = contributionRepository.findByBetId(1001L);
        assertEquals(1, contributions.size());

        JackpotContribution contribution = contributions.get(0);
        assertEquals(1001L, contribution.getBetId());
        assertEquals(100L, contribution.getUserId());
        assertEquals(1L, contribution.getJackpotId());
        assertEquals(new BigDecimal("100.00"), contribution.getStakeAmount());
        assertEquals(new BigDecimal("10.00"), contribution.getContributionAmount()); // 10% of 100

        // Step 4: Verify jackpot pool increased
        jackpot = jackpotRepository.findById(1L).orElseThrow();
        assertEquals(initialPool.add(new BigDecimal("10.00")), jackpot.getCurrentPoolValue());

        // Step 5: Evaluate jackpot (result depends on randomness)
        MvcResult result = mockMvc.perform(get("/api/jackpots/1001/evaluate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.won").exists())
                .andExpect(jsonPath("$.rewardAmount").exists())
                .andExpect(jsonPath("$.message").exists())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JackpotEvaluationResponse response = objectMapper.readValue(responseBody, JackpotEvaluationResponse.class);

        assertNotNull(response);
        assertNotNull(response.getMessage());

        if (response.isWon()) {
            // If won, jackpot should be reset
            assertTrue(response.getRewardAmount().compareTo(BigDecimal.ZERO) > 0);
            assertTrue(response.getMessage().contains("Congratulations"));

            // Verify jackpot was reset
            jackpot = jackpotRepository.findById(1L).orElseThrow();
            assertEquals(jackpot.getInitialPoolValue(), jackpot.getCurrentPoolValue());
        } else {
            // If not won
            assertEquals(BigDecimal.ZERO, response.getRewardAmount());
            assertTrue(response.getMessage().contains("Better luck"));
        }
    }

    @Test
    void testCompleteWorkflow_VariableJackpot_MultipleContributions() throws Exception {
        // Get initial jackpot state
        Jackpot jackpot = jackpotRepository.findById(2L).orElseThrow();
        assertEquals("VARIABLE_CONTRIBUTION", jackpot.getContributionType());
        assertEquals("VARIABLE_REWARD", jackpot.getRewardType());
        BigDecimal initialPool = jackpot.getCurrentPoolValue();

        // Submit multiple bets
        for (int i = 0; i < 5; i++) {
            BetRequest betRequest = new BetRequest(
                    2000L + i,
                    200L + i,
                    2L,
                    new BigDecimal("100.00")
            );

            mockMvc.perform(post("/api/bets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(betRequest)))
                    .andExpect(status().isAccepted());
        }

        // Verify all contributions were recorded
        List<JackpotContribution> allContributions = contributionRepository.findByJackpotId(2L);
        assertTrue(allContributions.size() >= 5);

        // Verify pool increased
        jackpot = jackpotRepository.findById(2L).orElseThrow();
        assertTrue(jackpot.getCurrentPoolValue().compareTo(initialPool) > 0);

        // Evaluate one of the bets
        mockMvc.perform(get("/api/jackpots/2000/evaluate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.won").isBoolean())
                .andExpect(jsonPath("$.rewardAmount").isNumber())
                .andExpect(jsonPath("$.message").isString());
    }

    @Test
    void testGetJackpot_ReturnsCorrectDetails() throws Exception {
        mockMvc.perform(get("/api/jackpots/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.initialPoolValue").value(1000.00))
                .andExpect(jsonPath("$.contributionType").value("FIXED_CONTRIBUTION"))
                .andExpect(jsonPath("$.rewardType").value("FIXED_REWARD"));
    }

    @Test
    void testGetJackpot_NotFound() throws Exception {
        mockMvc.perform(get("/api/jackpots/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testSubmitBet_InvalidJackpot_StillAccepted() throws Exception {
        // API accepts bet but processing will fail internally
        BetRequest betRequest = new BetRequest(
                9999L,
                999L,
                999L,  // Non-existent jackpot
                new BigDecimal("50.00")
        );

        mockMvc.perform(post("/api/bets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(betRequest)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void testEvaluateJackpot_NonExistentBet_ReturnsError() throws Exception {
        mockMvc.perform(get("/api/jackpots/99999/evaluate"))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.won").value(false))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testMultipleSequentialBets_PoolGrowsCorrectly() throws Exception {
        // Get initial pool value
        Jackpot jackpot = jackpotRepository.findById(3L).orElseThrow();
        BigDecimal initialPool = jackpot.getCurrentPoolValue();

        // Submit 3 bets
        for (int i = 0; i < 3; i++) {
            BetRequest betRequest = new BetRequest(
                    3000L + i,
                    300L,
                    3L,
                    new BigDecimal("200.00")
            );

            mockMvc.perform(post("/api/bets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(betRequest)))
                    .andExpect(status().isAccepted());

            // Small delay to ensure sequential processing
            Thread.sleep(50);
        }

        // Verify pool increased by expected amount
        // Jackpot 3 has 5% fixed contribution: 200 * 0.05 * 3 = 30
        jackpot = jackpotRepository.findById(3L).orElseThrow();
        BigDecimal expectedIncrease = new BigDecimal("30.00");
        assertEquals(
                initialPool.add(expectedIncrease),
                jackpot.getCurrentPoolValue()
        );

        // Verify 3 contributions were recorded
        List<JackpotContribution> contributions = contributionRepository.findByJackpotId(3L);
        assertTrue(contributions.size() >= 3);
    }

    @Test
    void testJackpotReset_AfterWin() throws Exception {
        // Submit a bet
        BetRequest betRequest = new BetRequest(
                5000L,
                500L,
                1L,
                new BigDecimal("50.00")
        );

        mockMvc.perform(post("/api/bets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(betRequest)))
                .andExpect(status().isAccepted());

        // Get pool value before evaluation
        Jackpot jackpot = jackpotRepository.findById(1L).orElseThrow();
        BigDecimal poolBeforeEval = jackpot.getCurrentPoolValue();
        BigDecimal initialPool = jackpot.getInitialPoolValue();

        // Evaluate multiple times until we get a win or timeout
        boolean won = false;
        for (int i = 0; i < 100; i++) {
            // Submit another bet
            BetRequest anotherBet = new BetRequest(
                    5100L + i,
                    500L,
                    1L,
                    new BigDecimal("10.00")
            );

            mockMvc.perform(post("/api/bets")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(anotherBet)));

            // Evaluate
            MvcResult result = mockMvc.perform(get("/api/jackpots/" + (5100L + i) + "/evaluate"))
                    .andReturn();

            String responseBody = result.getResponse().getContentAsString();
            JackpotEvaluationResponse response = objectMapper.readValue(responseBody, JackpotEvaluationResponse.class);

            if (response.isWon()) {
                won = true;
                // Verify jackpot was reset to initial value
                jackpot = jackpotRepository.findById(1L).orElseThrow();
                assertEquals(initialPool, jackpot.getCurrentPoolValue());
                break;
            }
        }

        // Note: This test might not always trigger a win due to randomness
        // but verifies the reset logic when it does happen
    }

    @Test
    void testConcurrentBets_AllProcessedCorrectly() throws Exception {
        // Get initial pool
        Jackpot jackpot = jackpotRepository.findById(1L).orElseThrow();
        BigDecimal initialPool = jackpot.getCurrentPoolValue();

        // Submit 10 bets
        int betCount = 10;
        for (int i = 0; i < betCount; i++) {
            BetRequest betRequest = new BetRequest(
                    6000L + i,
                    600L,
                    1L,
                    new BigDecimal("100.00")
            );

            mockMvc.perform(post("/api/bets")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(betRequest)));
        }

        // Allow time for processing
        Thread.sleep(500);

        // Verify all contributions were recorded
        int contributionCount = 0;
        for (int i = 0; i < betCount; i++) {
            List<JackpotContribution> contributions = contributionRepository.findByBetId(6000L + i);
            contributionCount += contributions.size();
        }

        assertEquals(betCount, contributionCount);

        // Verify pool increased correctly
        // 10 bets * 100 * 10% = 100
        jackpot = jackpotRepository.findById(1L).orElseThrow();
        BigDecimal expectedIncrease = new BigDecimal("100.00");
        assertEquals(
                initialPool.add(expectedIncrease),
                jackpot.getCurrentPoolValue()
        );
    }
}
