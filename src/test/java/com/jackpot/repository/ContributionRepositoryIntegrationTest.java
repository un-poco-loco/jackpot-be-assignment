package com.jackpot.repository;

import com.jackpot.model.JackpotContribution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for ContributionRepository.
 */
@DataJpaTest
@ActiveProfiles("test")
class ContributionRepositoryIntegrationTest {

    @Autowired
    private ContributionRepository contributionRepository;

    @BeforeEach
    void setUp() {
        contributionRepository.deleteAll();
    }

    @Test
    void testSave_NewContribution_Success() {
        // Given
        JackpotContribution contribution = JackpotContribution.builder()
                .betId(100L)
                .userId(200L)
                .jackpotId(1L)
                .stakeAmount(new BigDecimal("100.00"))
                .contributionAmount(new BigDecimal("10.00"))
                .currentJackpotAmount(new BigDecimal("1010.00"))
                .build();

        // When
        JackpotContribution saved = contributionRepository.save(contribution);

        // Then
        assertNotNull(saved.getId());
        assertEquals(100L, saved.getBetId());
        assertEquals(200L, saved.getUserId());
        assertEquals(1L, saved.getJackpotId());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void testFindByBetId_ExistingContribution_ReturnsContribution() {
        // Given
        JackpotContribution contribution = JackpotContribution.builder()
                .betId(101L)
                .userId(201L)
                .jackpotId(1L)
                .stakeAmount(new BigDecimal("50.00"))
                .contributionAmount(new BigDecimal("5.00"))
                .currentJackpotAmount(new BigDecimal("1005.00"))
                .build();
        contributionRepository.save(contribution);

        // When
        List<JackpotContribution> results = contributionRepository.findByBetId(101L);

        // Then
        assertEquals(1, results.size());
        assertEquals(101L, results.get(0).getBetId());
    }

    @Test
    void testFindByBetId_NonExistent_ReturnsEmpty() {
        // When
        List<JackpotContribution> results = contributionRepository.findByBetId(999L);

        // Then
        assertTrue(results.isEmpty());
    }

    @Test
    void testFindByJackpotId_MultipleContributions_ReturnsAll() {
        // Given
        for (int i = 0; i < 5; i++) {
            JackpotContribution contribution = JackpotContribution.builder()
                    .betId(200L + i)
                    .userId(300L)
                    .jackpotId(2L)
                    .stakeAmount(new BigDecimal("100.00"))
                    .contributionAmount(new BigDecimal("15.00"))
                    .currentJackpotAmount(new BigDecimal("5000.00"))
                    .build();
            contributionRepository.save(contribution);
        }

        // When
        List<JackpotContribution> results = contributionRepository.findByJackpotId(2L);

        // Then
        assertEquals(5, results.size());
        assertTrue(results.stream().allMatch(c -> c.getJackpotId().equals(2L)));
    }
}
