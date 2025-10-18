package com.jackpot.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FixedContributionStrategy.
 */
class FixedContributionStrategyTest {

    private FixedContributionStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new FixedContributionStrategy();
    }

    @Test
    void testCalculateContribution_10Percent() {
        // Given
        BigDecimal betAmount = new BigDecimal("100.00");
        BigDecimal currentPool = new BigDecimal("1000.00");
        String config = "{\"percentage\": 0.10}";

        // When
        BigDecimal contribution = strategy.calculateContribution(betAmount, currentPool, config);

        // Then
        assertEquals(new BigDecimal("10.00"), contribution);
    }

    @Test
    void testCalculateContribution_5Percent() {
        // Given
        BigDecimal betAmount = new BigDecimal("200.00");
        BigDecimal currentPool = new BigDecimal("2000.00");
        String config = "{\"percentage\": 0.05}";

        // When
        BigDecimal contribution = strategy.calculateContribution(betAmount, currentPool, config);

        // Then
        assertEquals(new BigDecimal("10.00"), contribution);
    }

    @Test
    void testCalculateContribution_RoundingHalfUp() {
        // Given
        BigDecimal betAmount = new BigDecimal("33.33");
        BigDecimal currentPool = new BigDecimal("1000.00");
        String config = "{\"percentage\": 0.10}";

        // When
        BigDecimal contribution = strategy.calculateContribution(betAmount, currentPool, config);

        // Then
        assertEquals(new BigDecimal("3.33"), contribution);
    }
}
