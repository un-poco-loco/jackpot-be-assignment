package com.jackpot.strategy;

import java.math.BigDecimal;

/**
 * Strategy interface for evaluating jackpot rewards.
 */
public interface RewardStrategy {

    /**
     * Evaluates if a bet wins the jackpot.
     *
     * @param currentPoolValue the current jackpot pool value
     * @param config configuration string (JSON format)
     * @return true if the bet wins the jackpot, false otherwise
     */
    boolean evaluateWin(BigDecimal currentPoolValue, String config);
}
