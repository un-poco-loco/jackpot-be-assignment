package com.jackpot.service;

import com.jackpot.kafka.KafkaProducer;
import com.jackpot.model.Bet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service for handling bet operations.
 */
@Service
public class BetService {

    private static final Logger logger = LoggerFactory.getLogger(BetService.class);

    private final KafkaProducer kafkaProducer;
    private final JackpotService jackpotService;
    private final boolean mockEnabled;

    public BetService(
            KafkaProducer kafkaProducer,
            JackpotService jackpotService,
            @Value("${jackpot.mock-enabled}") boolean mockEnabled) {
        this.kafkaProducer = kafkaProducer;
        this.jackpotService = jackpotService;
        this.mockEnabled = mockEnabled;
    }

    /**
     * Publishes a bet to Kafka for processing.
     * In mock mode, directly processes the bet.
     * Validates that the jackpot exists before publishing.
     *
     * @param bet the bet to publish
     * @throws RuntimeException if jackpot does not exist
     */
    public void publishBet(Bet bet) {
        logger.info("Publishing bet - BetId: {}, UserId: {}, JackpotId: {}, Amount: {}",
                bet.getBetId(), bet.getUserId(), bet.getJackpotId(), bet.getBetAmount());

        // Validate jackpot exists before publishing to Kafka
        jackpotService.getJackpot(bet.getJackpotId());

        kafkaProducer.sendBet(bet);

        // In mock mode, directly process the bet since Kafka is not active
        if (mockEnabled) {
            logger.info("Mock mode: Directly processing bet contribution");
            jackpotService.processBetContribution(bet);
        }
    }
}
