package com.jackpot.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for error handling in JackpotController.
 * Verifies proper HTTP status codes and error messages for various error scenarios.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class JackpotControllerErrorHandlingTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testEvaluateNonExistentBet_ShouldReturn404() throws Exception {
        // Given - bet ID that was never submitted
        Long nonExistentBetId = 999999L;

        // When & Then
        mockMvc.perform(get("/api/jackpots/{betId}/evaluate", nonExistentBetId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Bet not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testEvaluateNegativeBetId_ShouldReturn400() throws Exception {
        // Given - negative bet ID
        Long negativeBetId = -123L;

        // When & Then
        mockMvc.perform(get("/api/jackpots/{betId}/evaluate", negativeBetId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid bet ID"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid bet ID: -123. Bet ID must be a positive number."))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testEvaluateZeroBetId_ShouldReturn400() throws Exception {
        // Given - zero bet ID
        Long zeroBetId = 0L;

        // When & Then
        mockMvc.perform(get("/api/jackpots/{betId}/evaluate", zeroBetId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid bet ID"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid bet ID: 0. Bet ID must be a positive number."))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testEvaluateInvalidBetIdType_ShouldReturn400() throws Exception {
        // Given - invalid bet ID type (string instead of number)
        String invalidBetId = "abc";

        // When & Then
        mockMvc.perform(get("/api/jackpots/{betId}/evaluate", invalidBetId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid parameter type"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Parameter 'betId' must be a valid Long"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testGetNonExistentJackpot_ShouldReturn404() throws Exception {
        // Given - jackpot ID that doesn't exist
        Long nonExistentJackpotId = 999999L;

        // When & Then
        mockMvc.perform(get("/api/jackpots/{jackpotId}", nonExistentJackpotId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Jackpot not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Jackpot not found with ID: 999999"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testGetJackpotInvalidIdType_ShouldReturn400() throws Exception {
        // Given - invalid jackpot ID type
        String invalidJackpotId = "xyz";

        // When & Then
        mockMvc.perform(get("/api/jackpots/{jackpotId}", invalidJackpotId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid parameter type"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Parameter 'jackpotId' must be a valid Long"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testGetExistingJackpot_ShouldReturn200() throws Exception {
        // Given - existing jackpot ID (from data.sql)
        Long existingJackpotId = 1L;

        // When & Then
        mockMvc.perform(get("/api/jackpots/{jackpotId}", existingJackpotId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.initialPoolValue").exists())
                .andExpect(jsonPath("$.currentPoolValue").exists());
    }
}
