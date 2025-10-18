package com.jackpot.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Variable contribution strategy with decreasing percentage as pool grows.
 * The percentage decreases linearly as the pool approaches the limit.
 * Config format: {"basePercentage": 0.15, "poolLimit": 10000.00}
 */
@Component("VARIABLE_CONTRIBUTION")
public class VariableContributionStrategy implements ContributionStrategy {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public BigDecimal calculateContribution(BigDecimal betAmount, BigDecimal currentPoolValue, String config) {
        try {
            JsonNode configNode = objectMapper.readTree(config);
            double basePercentage = configNode.get("basePercentage").asDouble();
            BigDecimal poolLimit = new BigDecimal(configNode.get("poolLimit").asText());

            // Calculate percentage that decreases as pool grows
            // percentage = basePercentage * (1 - currentPool / poolLimit)
            BigDecimal poolRatio = currentPoolValue.divide(poolLimit, 4, RoundingMode.HALF_UP);
            BigDecimal adjustmentFactor = BigDecimal.ONE.subtract(poolRatio);

            // Ensure adjustment factor is not negative
            if (adjustmentFactor.compareTo(BigDecimal.ZERO) < 0) {
                adjustmentFactor = BigDecimal.ZERO;
            }

            double effectivePercentage = basePercentage * adjustmentFactor.doubleValue();

            return betAmount.multiply(BigDecimal.valueOf(effectivePercentage))
                    .setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse contribution config: " + config, e);
        }
    }
}
