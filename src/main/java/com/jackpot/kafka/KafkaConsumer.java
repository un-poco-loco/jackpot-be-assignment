package com.jackpot.kafka;

import com.jackpot.model.Bet;
import com.jackpot.service.JackpotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer for processing bet messages.
 * Only active when mock mode is disabled.
 */
@Component
public class KafkaConsumer {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumer.class);

    private final JackpotService jackpotService;
    private final boolean mockEnabled;

    public KafkaConsumer(
            JackpotService jackpotService,
            @Value("${jackpot.mock-enabled}") boolean mockEnabled) {
        this.jackpotService = jackpotService;
        this.mockEnabled = mockEnabled;
    }

    /**
     * Consumes bet messages from Kafka and processes jackpot contributions.
     *
     * @param bet the bet message
     */
    @KafkaListener(topics = "${jackpot.kafka.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeBet(Bet bet) {
        if (!mockEnabled) {
            logger.info("Received bet from Kafka - BetId: {}, JackpotId: {}", bet.getBetId(), bet.getJackpotId());
            try {
                jackpotService.processBetContribution(bet);
                logger.info("Successfully processed bet contribution - BetId: {}", bet.getBetId());
            } catch (Exception e) {
                logger.error("Error processing bet contribution - BetId: {}", bet.getBetId(), e);
            }
        }
    }
}
