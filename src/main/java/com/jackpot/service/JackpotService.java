package com.jackpot.service;

import com.jackpot.exception.BetNotFoundException;
import com.jackpot.exception.InvalidBetIdException;
import com.jackpot.exception.JackpotNotFoundException;
import com.jackpot.model.Bet;
import com.jackpot.model.Jackpot;
import com.jackpot.model.JackpotContribution;
import com.jackpot.model.JackpotReward;
import com.jackpot.repository.ContributionRepository;
import com.jackpot.repository.JackpotRepository;
import com.jackpot.repository.RewardRepository;
import com.jackpot.strategy.ContributionStrategy;
import com.jackpot.strategy.RewardStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Service for managing jackpot contributions and rewards.
 */
@Service
public class JackpotService {

    private static final Logger logger = LoggerFactory.getLogger(JackpotService.class);

    private final JackpotRepository jackpotRepository;
    private final ContributionRepository contributionRepository;
    private final RewardRepository rewardRepository;
    private final Map<String, ContributionStrategy> contributionStrategies;
    private final Map<String, RewardStrategy> rewardStrategies;

    public JackpotService(
            JackpotRepository jackpotRepository,
            ContributionRepository contributionRepository,
            RewardRepository rewardRepository,
            Map<String, ContributionStrategy> contributionStrategies,
            Map<String, RewardStrategy> rewardStrategies) {
        this.jackpotRepository = jackpotRepository;
        this.contributionRepository = contributionRepository;
        this.rewardRepository = rewardRepository;
        this.contributionStrategies = contributionStrategies;
        this.rewardStrategies = rewardStrategies;
    }

    /**
     * Processes a bet and adds contribution to the jackpot.
     *
     * @param bet the bet to process
     */
    @Transactional
    public void processBetContribution(Bet bet) {
        logger.info("Processing bet contribution - BetId: {}, JackpotId: {}", bet.getBetId(), bet.getJackpotId());

        Jackpot jackpot = jackpotRepository.findById(bet.getJackpotId())
                .orElseThrow(() -> new RuntimeException("Jackpot not found: " + bet.getJackpotId()));

        // Get the appropriate contribution strategy
        ContributionStrategy strategy = contributionStrategies.get(jackpot.getContributionType());
        if (strategy == null) {
            throw new RuntimeException("Unknown contribution strategy: " + jackpot.getContributionType());
        }

        // Calculate contribution
        BigDecimal contributionAmount = strategy.calculateContribution(
                bet.getBetAmount(),
                jackpot.getCurrentPoolValue(),
                jackpot.getContributionConfig()
        );

        logger.info("Calculated contribution: {} for bet: {}", contributionAmount, bet.getBetId());

        // Update jackpot pool
        BigDecimal newPoolValue = jackpot.getCurrentPoolValue().add(contributionAmount);
        jackpot.setCurrentPoolValue(newPoolValue);
        jackpotRepository.save(jackpot);

        // Save contribution record
        JackpotContribution contribution = JackpotContribution.builder()
                .betId(bet.getBetId())
                .userId(bet.getUserId())
                .jackpotId(bet.getJackpotId())
                .stakeAmount(bet.getBetAmount())
                .contributionAmount(contributionAmount)
                .currentJackpotAmount(newPoolValue)
                .build();

        contributionRepository.save(contribution);

        logger.info("Contribution processed successfully. New pool value: {}", newPoolValue);
    }

    /**
     * Evaluates if a bet wins the jackpot and processes the reward.
     *
     * @param betId the bet ID to evaluate
     * @return the reward amount if won, null otherwise
     * @throws InvalidBetIdException if bet ID is null or non-positive
     * @throws BetNotFoundException if no contribution found for the bet
     * @throws JackpotNotFoundException if jackpot not found
     */
    @Transactional
    public BigDecimal evaluateJackpotReward(Long betId) {
        logger.info("Evaluating jackpot reward for bet: {}", betId);

        // Validate bet ID
        if (betId == null || betId <= 0) {
            throw new InvalidBetIdException(betId);
        }

        // Find the contribution for this bet
        JackpotContribution contribution = contributionRepository.findByBetId(betId).stream()
                .findFirst()
                .orElseThrow(() -> new BetNotFoundException(betId));

        Jackpot jackpot = jackpotRepository.findById(contribution.getJackpotId())
                .orElseThrow(() -> new JackpotNotFoundException(contribution.getJackpotId()));

        // Get the appropriate reward strategy
        RewardStrategy strategy = rewardStrategies.get(jackpot.getRewardType());
        if (strategy == null) {
            throw new RuntimeException("Unknown reward strategy: " + jackpot.getRewardType());
        }

        // Evaluate if bet wins
        boolean won = strategy.evaluateWin(jackpot.getCurrentPoolValue(), jackpot.getRewardConfig());

        if (won) {
            logger.info("Bet {} WON the jackpot! Reward amount: {}", betId, jackpot.getCurrentPoolValue());

            BigDecimal rewardAmount = jackpot.getCurrentPoolValue();

            // Save reward record
            JackpotReward reward = JackpotReward.builder()
                    .betId(betId)
                    .userId(contribution.getUserId())
                    .jackpotId(jackpot.getId())
                    .jackpotRewardAmount(rewardAmount)
                    .build();

            rewardRepository.save(reward);

            // Reset jackpot to initial value
            jackpot.setCurrentPoolValue(jackpot.getInitialPoolValue());
            jackpotRepository.save(jackpot);

            logger.info("Jackpot reset to initial value: {}", jackpot.getInitialPoolValue());

            return rewardAmount;
        } else {
            logger.info("Bet {} did not win the jackpot", betId);
            return null;
        }
    }

    /**
     * Gets jackpot by ID.
     *
     * @param jackpotId the jackpot ID
     * @return the jackpot
     * @throws JackpotNotFoundException if jackpot not found
     */
    public Jackpot getJackpot(Long jackpotId) {
        return jackpotRepository.findById(jackpotId)
                .orElseThrow(() -> new JackpotNotFoundException(jackpotId));
    }
}
