package com.jackpot.kafka;

import com.jackpot.model.Bet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka producer for publishing bet messages.
 * Supports both real Kafka and mock mode.
 */
@Component
public class KafkaProducer {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducer.class);

    private final KafkaTemplate<String, Bet> kafkaTemplate;
    private final String topic;
    private final boolean mockEnabled;

    public KafkaProducer(
            KafkaTemplate<String, Bet> kafkaTemplate,
            @Value("${jackpot.kafka.topic}") String topic,
            @Value("${jackpot.mock-enabled}") boolean mockEnabled) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
        this.mockEnabled = mockEnabled;
    }

    /**
     * Sends a bet message to Kafka topic.
     *
     * @param bet the bet to publish
     */
    public void sendBet(Bet bet) {
        if (mockEnabled) {
            logger.info("MOCK MODE: Would send bet to Kafka - BetId: {}, UserId: {}, JackpotId: {}, Amount: {}",
                    bet.getBetId(), bet.getUserId(), bet.getJackpotId(), bet.getBetAmount());
        } else {
            logger.info("Sending bet to Kafka - BetId: {}, JackpotId: {}", bet.getBetId(), bet.getJackpotId());
            kafkaTemplate.send(topic, String.valueOf(bet.getBetId()), bet);
        }
    }
}
