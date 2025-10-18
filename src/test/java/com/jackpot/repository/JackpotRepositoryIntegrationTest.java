package com.jackpot.repository;

import com.jackpot.model.Jackpot;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for JackpotRepository.
 */
@DataJpaTest
@ActiveProfiles("test")
class JackpotRepositoryIntegrationTest {

    @Autowired
    private JackpotRepository jackpotRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void testFindById_ExistingJackpot_ReturnsJackpot() {
        // When
        Optional<Jackpot> result = jackpotRepository.findById(1L);

        // Then
        assertTrue(result.isPresent());
        Jackpot jackpot = result.get();
        assertEquals(1L, jackpot.getId());
        assertEquals(new BigDecimal("1000.00"), jackpot.getInitialPoolValue());
        assertEquals("FIXED_CONTRIBUTION", jackpot.getContributionType());
        assertEquals("FIXED_REWARD", jackpot.getRewardType());
    }

    @Test
    void testFindById_NonExistentJackpot_ReturnsEmpty() {
        // When
        Optional<Jackpot> result = jackpotRepository.findById(999L);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void testSave_UpdateExistingJackpot_Success() {
        // Given - Get an existing jackpot and update it
        Jackpot jackpot = jackpotRepository.findById(2L).orElseThrow();
        BigDecimal originalPool = jackpot.getCurrentPoolValue();
        BigDecimal newPoolValue = originalPool.add(new BigDecimal("500.00"));

        // When
        jackpot.setCurrentPoolValue(newPoolValue);
        Jackpot saved = jackpotRepository.save(jackpot);
        entityManager.flush();

        // Then
        assertNotNull(saved.getId());
        assertEquals(2L, saved.getId());
        assertEquals(newPoolValue, saved.getCurrentPoolValue());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());

        // Verify it persists
        entityManager.clear(); // Clear cache
        Jackpot retrieved = jackpotRepository.findById(2L).orElseThrow();
        assertEquals(newPoolValue, retrieved.getCurrentPoolValue());
    }

    @Test
    void testUpdate_ExistingJackpot_Success() {
        // Given
        Jackpot jackpot = jackpotRepository.findById(1L).orElseThrow();
        BigDecimal newPoolValue = new BigDecimal("1500.00");

        // When
        jackpot.setCurrentPoolValue(newPoolValue);
        Jackpot updated = jackpotRepository.save(jackpot);

        // Then
        assertEquals(newPoolValue, updated.getCurrentPoolValue());
        assertNotNull(updated.getUpdatedAt());
    }
}
