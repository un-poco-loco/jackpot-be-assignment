package com.jackpot.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

/**
 * Variable reward strategy with increasing win chance as pool grows.
 * The win probability increases linearly as the pool approaches the limit.
 * At the limit, win chance is 100%.
 * Config format: {"poolLimit": 10000.00}
 */
@Component("VARIABLE_REWARD")
public class VariableRewardStrategy implements RewardStrategy {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();

    @Override
    public boolean evaluateWin(BigDecimal currentPoolValue, String config) {
        try {
            JsonNode configNode = objectMapper.readTree(config);
            BigDecimal poolLimit = new BigDecimal(configNode.get("poolLimit").asText());

            // Calculate win percentage based on pool size
            // winChance = currentPool / poolLimit (0 to 1)
            BigDecimal winChance = currentPoolValue.divide(poolLimit, 4, RoundingMode.HALF_UP);

            // Ensure win chance is between 0 and 1
            if (winChance.compareTo(BigDecimal.ONE) > 0) {
                winChance = BigDecimal.ONE;
            }
            if (winChance.compareTo(BigDecimal.ZERO) < 0) {
                winChance = BigDecimal.ZERO;
            }

            // Generate random number between 0 and 1
            double randomValue = random.nextDouble();

            return randomValue < winChance.doubleValue();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse reward config: " + config, e);
        }
    }
}
