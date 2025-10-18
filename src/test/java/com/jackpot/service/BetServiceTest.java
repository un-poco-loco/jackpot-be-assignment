package com.jackpot.service;

import com.jackpot.kafka.KafkaProducer;
import com.jackpot.model.Bet;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BetService.
 * Tests verify that BetService correctly delegates to KafkaProducer and JackpotService
 * based on the mock-enabled configuration.
 */
class BetServiceTest {

    @Test
    void testPublishBet_MockModeEnabled_ProcessesDirectly() {
        // Given
        TestKafkaProducer kafkaProducer = new TestKafkaProducer();
        TestJackpotService jackpotService = new TestJackpotService();
        BetService betService = new BetService(kafkaProducer, jackpotService, true);
        Bet bet = new Bet(1L, 100L, 1L, new BigDecimal("100.00"));

        // When
        betService.publishBet(bet);

        // Then
        assertTrue(kafkaProducer.sendCalled.get(), "KafkaProducer.sendBet should be called");
        assertEquals(1, kafkaProducer.sendCount.get());
        assertTrue(jackpotService.processCalled.get(), "JackpotService.processBetContribution should be called in mock mode");
        assertEquals(1, jackpotService.processCount.get());
    }

    @Test
    void testPublishBet_MockModeDisabled_OnlySendsToKafka() {
        // Given
        TestKafkaProducer kafkaProducer = new TestKafkaProducer();
        TestJackpotService jackpotService = new TestJackpotService();
        BetService betService = new BetService(kafkaProducer, jackpotService, false);
        Bet bet = new Bet(2L, 200L, 2L, new BigDecimal("200.00"));

        // When
        betService.publishBet(bet);

        // Then
        assertTrue(kafkaProducer.sendCalled.get(), "KafkaProducer.sendBet should be called");
        assertEquals(1, kafkaProducer.sendCount.get());
        assertFalse(jackpotService.processCalled.get(), "JackpotService.processBetContribution should NOT be called when mock mode is disabled");
        assertEquals(0, jackpotService.processCount.get());
    }

    // Simple test implementations that track method calls
    private static class TestKafkaProducer extends KafkaProducer {
        AtomicBoolean sendCalled = new AtomicBoolean(false);
        AtomicInteger sendCount = new AtomicInteger(0);

        public TestKafkaProducer() {
            // Pass null to parent - we're overriding sendBet anyway
            super(null, "test-topic", true);
        }

        @Override
        public void sendBet(Bet bet) {
            // Override parent method - don't call super
            sendCalled.set(true);
            sendCount.incrementAndGet();
        }
    }

    private static class TestJackpotService extends JackpotService {
        AtomicBoolean processCalled = new AtomicBoolean(false);
        AtomicInteger processCount = new AtomicInteger(0);

        public TestJackpotService() {
            // Pass null to parent - we're overriding all methods anyway
            super(null, null, null, null, null);
        }

        @Override
        public void processBetContribution(Bet bet) {
            // Override parent method - don't call super
            processCalled.set(true);
            processCount.incrementAndGet();
        }

        @Override
        public com.jackpot.model.Jackpot getJackpot(Long jackpotId) {
            // Return a dummy jackpot for validation - don't call super
            com.jackpot.model.Jackpot jackpot = new com.jackpot.model.Jackpot();
            jackpot.setId(jackpotId);
            return jackpot;
        }
    }
}
