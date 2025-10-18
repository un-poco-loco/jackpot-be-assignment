package com.jackpot.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Fixed percentage contribution strategy.
 * Contributes a fixed percentage of each bet to the jackpot.
 * Config format: {"percentage": 0.10} for 10%
 */
@Component("FIXED_CONTRIBUTION")
public class FixedContributionStrategy implements ContributionStrategy {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public BigDecimal calculateContribution(BigDecimal betAmount, BigDecimal currentPoolValue, String config) {
        try {
            JsonNode configNode = objectMapper.readTree(config);
            double percentage = configNode.get("percentage").asDouble();

            return betAmount.multiply(BigDecimal.valueOf(percentage))
                    .setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse contribution config: " + config, e);
        }
    }
}
