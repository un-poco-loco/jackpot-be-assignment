package com.jackpot.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for VariableContributionStrategy.
 */
class VariableContributionStrategyTest {

    private VariableContributionStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new VariableContributionStrategy();
    }

    @Test
    void testCalculateContribution_EmptyPool() {
        // Given - pool is at 0% capacity
        BigDecimal betAmount = new BigDecimal("100.00");
        BigDecimal currentPool = new BigDecimal("0.00");
        String config = "{\"basePercentage\": 0.15, \"poolLimit\": 10000.00}";

        // When
        BigDecimal contribution = strategy.calculateContribution(betAmount, currentPool, config);

        // Then - should use full 15% (100 * 0.15 = 15.00)
        assertEquals(new BigDecimal("15.00"), contribution);
    }

    @Test
    void testCalculateContribution_HalfCapacity() {
        // Given - pool is at 50% capacity
        BigDecimal betAmount = new BigDecimal("100.00");
        BigDecimal currentPool = new BigDecimal("5000.00");
        String config = "{\"basePercentage\": 0.15, \"poolLimit\": 10000.00}";

        // When
        BigDecimal contribution = strategy.calculateContribution(betAmount, currentPool, config);

        // Then - should use 7.5% (100 * 0.15 * 0.5 = 7.50)
        assertEquals(new BigDecimal("7.50"), contribution);
    }

    @Test
    void testCalculateContribution_NearLimit() {
        // Given - pool is at 90% capacity
        BigDecimal betAmount = new BigDecimal("100.00");
        BigDecimal currentPool = new BigDecimal("9000.00");
        String config = "{\"basePercentage\": 0.15, \"poolLimit\": 10000.00}";

        // When
        BigDecimal contribution = strategy.calculateContribution(betAmount, currentPool, config);

        // Then - should use 1.5% (100 * 0.15 * 0.1 = 1.50)
        assertEquals(new BigDecimal("1.50"), contribution);
    }

    @Test
    void testCalculateContribution_AtLimit() {
        // Given - pool is at 100% capacity
        BigDecimal betAmount = new BigDecimal("100.00");
        BigDecimal currentPool = new BigDecimal("10000.00");
        String config = "{\"basePercentage\": 0.15, \"poolLimit\": 10000.00}";

        // When
        BigDecimal contribution = strategy.calculateContribution(betAmount, currentPool, config);

        // Then - should be 0%
        assertEquals(new BigDecimal("0.00"), contribution);
    }
}
