package com.jackpot.repository;

import com.jackpot.model.JackpotReward;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for RewardRepository.
 */
@DataJpaTest
@ActiveProfiles("test")
class RewardRepositoryIntegrationTest {

    @Autowired
    private RewardRepository rewardRepository;

    @BeforeEach
    void setUp() {
        rewardRepository.deleteAll();
    }

    @Test
    void testSave_NewReward_Success() {
        // Given
        JackpotReward reward = JackpotReward.builder()
                .betId(500L)
                .userId(600L)
                .jackpotId(1L)
                .jackpotRewardAmount(new BigDecimal("5000.00"))
                .build();

        // When
        JackpotReward saved = rewardRepository.save(reward);

        // Then
        assertNotNull(saved.getId());
        assertEquals(500L, saved.getBetId());
        assertEquals(600L, saved.getUserId());
        assertEquals(1L, saved.getJackpotId());
        assertEquals(new BigDecimal("5000.00"), saved.getJackpotRewardAmount());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void testFindByBetId_ExistingReward_ReturnsReward() {
        // Given
        JackpotReward reward = JackpotReward.builder()
                .betId(501L)
                .userId(601L)
                .jackpotId(2L)
                .jackpotRewardAmount(new BigDecimal("10000.00"))
                .build();
        rewardRepository.save(reward);

        // When
        List<JackpotReward> results = rewardRepository.findByBetId(501L);

        // Then
        assertEquals(1, results.size());
        assertEquals(501L, results.get(0).getBetId());
        assertEquals(new BigDecimal("10000.00"), results.get(0).getJackpotRewardAmount());
    }

    @Test
    void testFindByBetId_NonExistent_ReturnsEmpty() {
        // When
        List<JackpotReward> results = rewardRepository.findByBetId(999L);

        // Then
        assertTrue(results.isEmpty());
    }

    @Test
    void testFindByJackpotId_MultipleRewards_ReturnsAll() {
        // Given
        for (int i = 0; i < 3; i++) {
            JackpotReward reward = JackpotReward.builder()
                    .betId(600L + i)
                    .userId(700L + i)
                    .jackpotId(1L)
                    .jackpotRewardAmount(new BigDecimal("2000.00"))
                    .build();
            rewardRepository.save(reward);
        }

        // When
        List<JackpotReward> results = rewardRepository.findByJackpotId(1L);

        // Then
        assertEquals(3, results.size());
        assertTrue(results.stream().allMatch(r -> r.getJackpotId().equals(1L)));
    }
}
