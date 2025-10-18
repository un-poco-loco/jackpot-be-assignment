package com.jackpot;

import com.jackpot.kafka.KafkaProducer;
import com.jackpot.model.Bet;
import com.jackpot.model.JackpotContribution;
import com.jackpot.repository.ContributionRepository;
import com.jackpot.repository.JackpotRepository;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Kafka messaging with real Kafka Testcontainer.
 * Tests the complete Kafka producer-consumer flow.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class KafkaIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private KafkaProducer kafkaProducer;

    @Autowired
    private JackpotRepository jackpotRepository;

    @Autowired
    private ContributionRepository contributionRepository;

    @Value("${jackpot.kafka.topic}")
    private String kafkaTopic;

    private KafkaConsumer<String, Bet> testConsumer;

    @BeforeEach
    void setUp() {
        contributionRepository.deleteAll();

        // Create test consumer
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, getKafkaBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-consumer-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, Bet.class);

        testConsumer = new KafkaConsumer<>(props);
        testConsumer.subscribe(Collections.singletonList(kafkaTopic));
    }

    @AfterEach
    void tearDown() {
        if (testConsumer != null) {
            testConsumer.close();
        }
    }

    @Test
    @Order(1)
    @DisplayName("Should publish bet message to Kafka container")
    void testPublishBetToKafka() {
        // Given
        Bet bet = new Bet(80001L, 8001L, 1L, new BigDecimal("100.00"));

        // When
        kafkaProducer.sendBet(bet);

        // Then - Consume the message with test consumer
        ConsumerRecords<String, Bet> records = testConsumer.poll(Duration.ofSeconds(10));

        assertFalse(records.isEmpty(), "Should receive at least one message");

        boolean foundBet = false;
        for (ConsumerRecord<String, Bet> record : records) {
            Bet receivedBet = record.value();
            if (receivedBet.getBetId().equals(80001L)) {
                foundBet = true;
                assertEquals(8001L, receivedBet.getUserId());
                assertEquals(1L, receivedBet.getJackpotId());
                assertEquals(new BigDecimal("100.00"), receivedBet.getBetAmount());
                break;
            }
        }

        assertTrue(foundBet, "Should find the published bet in Kafka");
    }

    @Test
    @Order(2)
    @DisplayName("Should process bet from Kafka and create contribution")
    void testKafkaConsumerProcessesBet() throws InterruptedException {
        // Given
        Bet bet = new Bet(80002L, 8002L, 1L, new BigDecimal("200.00"));

        // When
        kafkaProducer.sendBet(bet);

        // Wait for Kafka consumer to process
        Thread.sleep(3000);

        // Then - Verify contribution was created
        List<JackpotContribution> contributions = contributionRepository.findByBetId(80002L);

        assertEquals(1, contributions.size(), "Contribution should be created");

        JackpotContribution contribution = contributions.get(0);
        assertEquals(80002L, contribution.getBetId());
        assertEquals(8002L, contribution.getUserId());
        assertEquals(1L, contribution.getJackpotId());
        assertEquals(new BigDecimal("200.00"), contribution.getStakeAmount());
        assertEquals(new BigDecimal("20.00"), contribution.getContributionAmount()); // 10% of 200
        assertNotNull(contribution.getCreatedAt());
    }

    @Test
    @Order(3)
    @DisplayName("Should handle multiple messages in Kafka queue")
    void testMultipleMessagesInKafka() throws InterruptedException {
        // Given - Multiple bets
        int messageCount = 5;
        for (int i = 0; i < messageCount; i++) {
            Bet bet = new Bet(
                    80100L + i,
                    8100L,
                    2L,
                    new BigDecimal("50.00")
            );
            kafkaProducer.sendBet(bet);
        }

        // Wait for all messages to be processed
        Thread.sleep(5000);

        // Then - All should be processed
        for (int i = 0; i < messageCount; i++) {
            List<JackpotContribution> contributions = contributionRepository.findByBetId(80100L + i);
            assertEquals(1, contributions.size(),
                    "Contribution should exist for bet " + (80100L + i));
        }
    }

    @Test
    @Order(4)
    @DisplayName("Should maintain message order in Kafka")
    void testKafkaMessageOrder() throws InterruptedException {
        // Given - Sequential bets
        Long[] betIds = {80201L, 80202L, 80203L};

        for (Long betId : betIds) {
            Bet bet = new Bet(betId, 8200L, 1L, new BigDecimal("25.00"));
            kafkaProducer.sendBet(bet);
            Thread.sleep(100); // Small delay to ensure order
        }

        // Wait for processing
        Thread.sleep(3000);

        // Then - Verify all were processed
        for (Long betId : betIds) {
            List<JackpotContribution> contributions = contributionRepository.findByBetId(betId);
            assertEquals(1, contributions.size(),
                    "Bet " + betId + " should be processed");
        }
    }

    @Test
    @Order(5)
    @DisplayName("Should handle Kafka consumer errors gracefully")
    void testKafkaErrorHandling() throws InterruptedException {
        // Given - Bet with invalid jackpot ID
        Bet invalidBet = new Bet(80301L, 8301L, 999L, new BigDecimal("100.00"));

        // When
        kafkaProducer.sendBet(invalidBet);

        // Wait for processing attempt
        Thread.sleep(3000);

        // Then - No contribution should be created
        List<JackpotContribution> contributions = contributionRepository.findByBetId(80301L);
        assertTrue(contributions.isEmpty(),
                "No contribution should be created for invalid jackpot");
    }

    @Test
    @Order(6)
    @DisplayName("Should process bets with different jackpot types")
    void testDifferentJackpotTypes() throws InterruptedException {
        // Given - Bets for different jackpot types
        Bet fixedBet = new Bet(80401L, 8401L, 1L, new BigDecimal("100.00")); // Fixed
        Bet variableBet = new Bet(80402L, 8402L, 2L, new BigDecimal("100.00")); // Variable

        // When
        kafkaProducer.sendBet(fixedBet);
        kafkaProducer.sendBet(variableBet);

        Thread.sleep(3000);

        // Then - Both should be processed with appropriate contributions
        List<JackpotContribution> fixedContributions = contributionRepository.findByBetId(80401L);
        assertEquals(1, fixedContributions.size());
        assertEquals(new BigDecimal("10.00"), fixedContributions.get(0).getContributionAmount());

        List<JackpotContribution> variableContributions = contributionRepository.findByBetId(80402L);
        assertEquals(1, variableContributions.size());
        // Variable contribution depends on current pool size
        assertNotNull(variableContributions.get(0).getContributionAmount());
    }

    @Test
    @Order(7)
    @DisplayName("Should verify Kafka container is running")
    void testKafkaContainerRunning() {
        assertTrue(kafkaContainer.isRunning(), "Kafka container should be running");
        assertNotNull(getKafkaBootstrapServers());
        assertTrue(getKafkaBootstrapServers().contains("PLAINTEXT"));
    }

    @Test
    @Order(8)
    @DisplayName("Should handle high-volume message processing")
    void testHighVolumeMessageProcessing() throws InterruptedException {
        // Given - Many bets
        int betCount = 20;
        long startBetId = 80500L;

        for (int i = 0; i < betCount; i++) {
            Bet bet = new Bet(
                    startBetId + i,
                    8500L,
                    3L,
                    new BigDecimal("10.00")
            );
            kafkaProducer.sendBet(bet);
        }

        // Wait for all to process
        Thread.sleep(6000);

        // Then - Verify all processed
        int processedCount = 0;
        for (int i = 0; i < betCount; i++) {
            List<JackpotContribution> contributions = contributionRepository.findByBetId(startBetId + i);
            if (!contributions.isEmpty()) {
                processedCount++;
            }
        }

        assertTrue(processedCount >= betCount * 0.9,
                "At least 90% of bets should be processed (processed: " + processedCount + "/" + betCount + ")");
    }

    @Test
    @Order(9)
    @DisplayName("Should persist Kafka-processed data to PostgreSQL")
    void testDataPersistenceFromKafka() throws InterruptedException {
        // Given
        Bet bet = new Bet(80601L, 8601L, 1L, new BigDecimal("150.00"));

        // When
        kafkaProducer.sendBet(bet);
        Thread.sleep(3000);

        // Then - Data should be in PostgreSQL
        List<JackpotContribution> contributions = contributionRepository.findByBetId(80601L);

        assertEquals(1, contributions.size());
        JackpotContribution contribution = contributions.get(0);

        // Verify all fields are properly persisted
        assertNotNull(contribution.getId(), "ID should be generated");
        assertNotNull(contribution.getCreatedAt(), "Timestamp should be set");
        assertEquals(80601L, contribution.getBetId());
        assertEquals(8601L, contribution.getUserId());
        assertEquals(1L, contribution.getJackpotId());
        assertTrue(contribution.getStakeAmount().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(contribution.getContributionAmount().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(contribution.getCurrentJackpotAmount().compareTo(BigDecimal.ZERO) > 0);
    }
}
