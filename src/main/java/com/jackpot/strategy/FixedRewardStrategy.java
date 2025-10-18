package com.jackpot.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Random;

/**
 * Fixed percentage chance reward strategy.
 * Each bet has a fixed probability of winning the jackpot.
 * Config format: {"percentage": 0.05} for 5% chance
 */
@Component("FIXED_REWARD")
public class FixedRewardStrategy implements RewardStrategy {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();

    @Override
    public boolean evaluateWin(BigDecimal currentPoolValue, String config) {
        try {
            JsonNode configNode = objectMapper.readTree(config);
            double winPercentage = configNode.get("percentage").asDouble();

            // Generate random number between 0 and 1
            double randomValue = random.nextDouble();

            return randomValue < winPercentage;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse reward config: " + config, e);
        }
    }
}
