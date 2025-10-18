package com.jackpot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jackpot.dto.BetRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for input validation in BetController.
 * Verifies that negative numbers and invalid inputs are properly rejected.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BetControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testNegativeBetId_ShouldReturnBadRequest() throws Exception {
        // Given - bet with negative betId
        BetRequest betRequest = new BetRequest(
                -123L,  // Negative bet ID
                1001L,
                1L,
                new BigDecimal("100.00")
        );

        // When & Then
        mockMvc.perform(post("/api/bets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(betRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.details.betId").value("Bet ID must be positive"));
    }

    @Test
    void testNegativeUserId_ShouldReturnBadRequest() throws Exception {
        // Given - bet with negative userId
        BetRequest betRequest = new BetRequest(
                123L,
                -1001L,  // Negative user ID
                1L,
                new BigDecimal("100.00")
        );

        // When & Then
        mockMvc.perform(post("/api/bets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(betRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.details.userId").value("User ID must be positive"));
    }

    @Test
    void testNegativeJackpotId_ShouldReturnBadRequest() throws Exception {
        // Given - bet with negative jackpotId
        BetRequest betRequest = new BetRequest(
                123L,
                1001L,
                -1L,  // Negative jackpot ID
                new BigDecimal("100.00")
        );

        // When & Then
        mockMvc.perform(post("/api/bets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(betRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.details.jackpotId").value("Jackpot ID must be positive"));
    }

    @Test
    void testNegativeBetAmount_ShouldReturnBadRequest() throws Exception {
        // Given - bet with negative amount
        BetRequest betRequest = new BetRequest(
                123L,
                1001L,
                1L,
                new BigDecimal("-100.00")  // Negative bet amount
        );

        // When & Then
        mockMvc.perform(post("/api/bets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(betRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.details.betAmount").value("Bet amount must be at least 0.01"));
    }

    @Test
    void testZeroBetAmount_ShouldReturnBadRequest() throws Exception {
        // Given - bet with zero amount
        BetRequest betRequest = new BetRequest(
                123L,
                1001L,
                1L,
                new BigDecimal("0.00")  // Zero bet amount
        );

        // When & Then
        mockMvc.perform(post("/api/bets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(betRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.details.betAmount").value("Bet amount must be at least 0.01"));
    }

    @Test
    void testZeroBetId_ShouldReturnBadRequest() throws Exception {
        // Given - bet with zero betId
        BetRequest betRequest = new BetRequest(
                0L,  // Zero bet ID
                1001L,
                1L,
                new BigDecimal("100.00")
        );

        // When & Then
        mockMvc.perform(post("/api/bets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(betRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.details.betId").value("Bet ID must be positive"));
    }

    @Test
    void testNullBetId_ShouldReturnBadRequest() throws Exception {
        // Given - bet with null betId
        BetRequest betRequest = new BetRequest(
                null,  // Null bet ID
                1001L,
                1L,
                new BigDecimal("100.00")
        );

        // When & Then
        mockMvc.perform(post("/api/bets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(betRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.details.betId").value("Bet ID is required"));
    }

    @Test
    void testNullBetAmount_ShouldReturnBadRequest() throws Exception {
        // Given - bet with null amount
        BetRequest betRequest = new BetRequest(
                123L,
                1001L,
                1L,
                null  // Null bet amount
        );

        // When & Then
        mockMvc.perform(post("/api/bets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(betRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.details.betAmount").value("Bet amount is required"));
    }

    @Test
    void testMultipleValidationErrors_ShouldReturnAllErrors() throws Exception {
        // Given - bet with multiple validation errors
        BetRequest betRequest = new BetRequest(
                -123L,  // Negative bet ID
                -1001L,  // Negative user ID
                -1L,  // Negative jackpot ID
                new BigDecimal("-100.00")  // Negative bet amount
        );

        // When & Then
        mockMvc.perform(post("/api/bets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(betRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.details.betId").exists())
                .andExpect(jsonPath("$.details.userId").exists())
                .andExpect(jsonPath("$.details.jackpotId").exists())
                .andExpect(jsonPath("$.details.betAmount").exists());
    }

    @Test
    void testValidBet_ShouldAccept() throws Exception {
        // Given - valid bet
        BetRequest betRequest = new BetRequest(
                123L,
                1001L,
                1L,
                new BigDecimal("100.00")
        );

        // When & Then
        mockMvc.perform(post("/api/bets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(betRequest)))
                .andExpect(status().isAccepted())  // 202 Accepted
                .andExpect(content().string("Bet published successfully"));
    }

    @Test
    void testMinimumValidBetAmount_ShouldAccept() throws Exception {
        // Given - bet with minimum valid amount (0.01)
        BetRequest betRequest = new BetRequest(
                123L,
                1001L,
                1L,
                new BigDecimal("0.01")  // Minimum valid amount
        );

        // When & Then
        mockMvc.perform(post("/api/bets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(betRequest)))
                .andExpect(status().isAccepted());
    }
}
