package com.jackpot.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for VariableRewardStrategy.
 */
class VariableRewardStrategyTest {

    private VariableRewardStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new VariableRewardStrategy();
    }

    @Test
    void testEvaluateWin_EmptyPool() {
        // Given - pool is at 0% capacity
        BigDecimal currentPool = new BigDecimal("0.00");
        String config = "{\"poolLimit\": 10000.00}";

        // When
        boolean won = strategy.evaluateWin(currentPool, config);

        // Then - should never win (0% chance)
        assertFalse(won);
    }

    @Test
    void testEvaluateWin_AtLimit() {
        // Given - pool is at 100% capacity
        BigDecimal currentPool = new BigDecimal("10000.00");
        String config = "{\"poolLimit\": 10000.00}";

        // When
        boolean won = strategy.evaluateWin(currentPool, config);

        // Then - should always win (100% chance)
        assertTrue(won);
    }

    @Test
    void testEvaluateWin_OverLimit() {
        // Given - pool is over 100% capacity
        BigDecimal currentPool = new BigDecimal("15000.00");
        String config = "{\"poolLimit\": 10000.00}";

        // When
        boolean won = strategy.evaluateWin(currentPool, config);

        // Then - should always win (capped at 100% chance)
        assertTrue(won);
    }

    @Test
    void testEvaluateWin_HalfCapacity_MultipleTrials() {
        // Given - pool is at 50% capacity
        BigDecimal currentPool = new BigDecimal("5000.00");
        String config = "{\"poolLimit\": 10000.00}";

        // When - run multiple trials
        int wins = 0;
        int trials = 1000;
        for (int i = 0; i < trials; i++) {
            if (strategy.evaluateWin(currentPool, config)) {
                wins++;
            }
        }

        // Then - wins should be approximately 50% (with some variance)
        // Allow range of 45% to 55% (reasonable for 1000 trials)
        double winRate = (double) wins / trials;
        assertTrue(winRate >= 0.45 && winRate <= 0.55,
                "Win rate should be approximately 50%, but was " + (winRate * 100) + "%");
    }
}
