package com.jackpot.controller;

import com.jackpot.model.Bet;
import com.jackpot.service.JackpotService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for JackpotController.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class JackpotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JackpotService jackpotService;

    @Test
    void testGetJackpot_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/jackpots/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.initialPoolValue").exists())
                .andExpect(jsonPath("$.currentPoolValue").exists());
    }

    @Test
    void testGetJackpot_NotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/jackpots/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testEvaluateJackpot_AfterBetPlaced() throws Exception {
        // Given - place a bet first
        Bet bet = new Bet(
                201L,
                2001L,
                1L,
                new BigDecimal("50.00")
        );
        jackpotService.processBetContribution(bet);

        // When & Then
        mockMvc.perform(get("/api/jackpots/201/evaluate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.won").exists())
                .andExpect(jsonPath("$.rewardAmount").exists())
                .andExpect(jsonPath("$.message").exists());
    }
}
