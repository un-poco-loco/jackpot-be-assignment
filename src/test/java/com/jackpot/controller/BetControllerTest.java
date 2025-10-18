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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for BetController.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testPublishBet_Success() throws Exception {
        // Given
        BetRequest betRequest = new BetRequest(
                101L,
                1001L,
                1L,
                new BigDecimal("100.00")
        );

        // When & Then
        mockMvc.perform(post("/api/bets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(betRequest)))
                .andExpect(status().isAccepted())
                .andExpect(content().string("Bet published successfully"));
    }

    @Test
    void testPublishBet_WithVariableContribution() throws Exception {
        // Given
        BetRequest betRequest = new BetRequest(
                102L,
                1002L,
                2L,
                new BigDecimal("200.00")
        );

        // When & Then
        mockMvc.perform(post("/api/bets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(betRequest)))
                .andExpect(status().isAccepted())
                .andExpect(content().string("Bet published successfully"));
    }
}
