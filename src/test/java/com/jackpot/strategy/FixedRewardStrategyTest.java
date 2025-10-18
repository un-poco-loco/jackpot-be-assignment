package com.jackpot.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FixedRewardStrategy.
 */
class FixedRewardStrategyTest {

    private FixedRewardStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new FixedRewardStrategy();
    }

    @Test
    void testEvaluateWin_100PercentChance() {
        // Given - 100% win chance
        BigDecimal currentPool = new BigDecimal("5000.00");
        String config = "{\"percentage\": 1.0}";

        // When
        boolean won = strategy.evaluateWin(currentPool, config);

        // Then - should always win
        assertTrue(won);
    }

    @Test
    void testEvaluateWin_0PercentChance() {
        // Given - 0% win chance
        BigDecimal currentPool = new BigDecimal("5000.00");
        String config = "{\"percentage\": 0.0}";

        // When
        boolean won = strategy.evaluateWin(currentPool, config);

        // Then - should never win
        assertFalse(won);
    }

    @Test
    void testEvaluateWin_5PercentChance_MultipleTrials() {
        // Given - 5% win chance
        BigDecimal currentPool = new BigDecimal("5000.00");
        String config = "{\"percentage\": 0.05}";

        // When - run multiple trials
        int wins = 0;
        int trials = 1000;
        for (int i = 0; i < trials; i++) {
            if (strategy.evaluateWin(currentPool, config)) {
                wins++;
            }
        }

        // Then - wins should be approximately 5% (with some variance)
        // Allow range of 2.5% to 7.5% (reasonable for 1000 trials)
        double winRate = (double) wins / trials;
        assertTrue(winRate >= 0.025 && winRate <= 0.075,
                "Win rate should be approximately 5%, but was " + (winRate * 100) + "%");
    }
}
