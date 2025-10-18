package com.jackpot.strategy;

import java.math.BigDecimal;

/**
 * Strategy interface for calculating jackpot contributions from bets.
 */
public interface ContributionStrategy {

    /**
     * Calculates the contribution amount from a bet.
     *
     * @param betAmount the amount of the bet
     * @param currentPoolValue the current jackpot pool value
     * @param config configuration string (JSON format)
     * @return the contribution amount to add to the jackpot
     */
    BigDecimal calculateContribution(BigDecimal betAmount, BigDecimal currentPoolValue, String config);
}
