package com.jackpot.service;

import com.jackpot.model.Bet;
import com.jackpot.model.Jackpot;
import com.jackpot.model.JackpotContribution;
import com.jackpot.repository.ContributionRepository;
import com.jackpot.repository.JackpotRepository;
import com.jackpot.repository.RewardRepository;
import com.jackpot.strategy.ContributionStrategy;
import com.jackpot.strategy.RewardStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for JackpotService.
 */
@ExtendWith(MockitoExtension.class)
class JackpotServiceTest {

    @Mock
    private JackpotRepository jackpotRepository;

    @Mock
    private ContributionRepository contributionRepository;

    @Mock
    private RewardRepository rewardRepository;

    @Mock
    private ContributionStrategy fixedContributionStrategy;

    @Mock
    private ContributionStrategy variableContributionStrategy;

    @Mock
    private RewardStrategy fixedRewardStrategy;

    @Mock
    private RewardStrategy variableRewardStrategy;

    private JackpotService jackpotService;

    @BeforeEach
    void setUp() {
        Map<String, ContributionStrategy> contributionStrategies = new HashMap<>();
        contributionStrategies.put("FIXED_CONTRIBUTION", fixedContributionStrategy);
        contributionStrategies.put("VARIABLE_CONTRIBUTION", variableContributionStrategy);

        Map<String, RewardStrategy> rewardStrategies = new HashMap<>();
        rewardStrategies.put("FIXED_REWARD", fixedRewardStrategy);
        rewardStrategies.put("VARIABLE_REWARD", variableRewardStrategy);

        jackpotService = new JackpotService(
                jackpotRepository,
                contributionRepository,
                rewardRepository,
                contributionStrategies,
                rewardStrategies
        );
    }

    @Test
    void testProcessBetContribution_FixedStrategy_Success() {
        // Given
        Bet bet = new Bet(1L, 100L, 1L, new BigDecimal("100.00"));

        Jackpot jackpot = new Jackpot();
        jackpot.setId(1L);
        jackpot.setCurrentPoolValue(new BigDecimal("1000.00"));
        jackpot.setContributionType("FIXED_CONTRIBUTION");
        jackpot.setContributionConfig("{\"percentage\": 0.10}");

        when(jackpotRepository.findById(1L)).thenReturn(Optional.of(jackpot));
        when(fixedContributionStrategy.calculateContribution(
                eq(new BigDecimal("100.00")),
                eq(new BigDecimal("1000.00")),
                eq("{\"percentage\": 0.10}")
        )).thenReturn(new BigDecimal("10.00"));

        // When
        jackpotService.processBetContribution(bet);

        // Then
        ArgumentCaptor<Jackpot> jackpotCaptor = ArgumentCaptor.forClass(Jackpot.class);
        verify(jackpotRepository).save(jackpotCaptor.capture());
        assertEquals(new BigDecimal("1010.00"), jackpotCaptor.getValue().getCurrentPoolValue());

        ArgumentCaptor<JackpotContribution> contributionCaptor = ArgumentCaptor.forClass(JackpotContribution.class);
        verify(contributionRepository).save(contributionCaptor.capture());

        JackpotContribution savedContribution = contributionCaptor.getValue();
        assertEquals(1L, savedContribution.getBetId());
        assertEquals(100L, savedContribution.getUserId());
        assertEquals(1L, savedContribution.getJackpotId());
        assertEquals(new BigDecimal("100.00"), savedContribution.getStakeAmount());
        assertEquals(new BigDecimal("10.00"), savedContribution.getContributionAmount());
        assertEquals(new BigDecimal("1010.00"), savedContribution.getCurrentJackpotAmount());
    }

    @Test
    void testProcessBetContribution_VariableStrategy_Success() {
        // Given
        Bet bet = new Bet(2L, 200L, 2L, new BigDecimal("200.00"));

        Jackpot jackpot = new Jackpot();
        jackpot.setId(2L);
        jackpot.setCurrentPoolValue(new BigDecimal("5000.00"));
        jackpot.setContributionType("VARIABLE_CONTRIBUTION");
        jackpot.setContributionConfig("{\"basePercentage\": 0.15, \"poolLimit\": 10000.00}");

        when(jackpotRepository.findById(2L)).thenReturn(Optional.of(jackpot));
        when(variableContributionStrategy.calculateContribution(
                eq(new BigDecimal("200.00")),
                eq(new BigDecimal("5000.00")),
                eq("{\"basePercentage\": 0.15, \"poolLimit\": 10000.00}")
        )).thenReturn(new BigDecimal("15.00"));

        // When
        jackpotService.processBetContribution(bet);

        // Then
        verify(jackpotRepository).save(any(Jackpot.class));
        verify(contributionRepository).save(any(JackpotContribution.class));
    }

    @Test
    void testProcessBetContribution_JackpotNotFound_ThrowsException() {
        // Given
        Bet bet = new Bet(1L, 100L, 999L, new BigDecimal("100.00"));
        when(jackpotRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> jackpotService.processBetContribution(bet));
        assertEquals("Jackpot not found: 999", exception.getMessage());
    }

    @Test
    void testProcessBetContribution_UnknownStrategy_ThrowsException() {
        // Given
        Bet bet = new Bet(1L, 100L, 1L, new BigDecimal("100.00"));

        Jackpot jackpot = new Jackpot();
        jackpot.setId(1L);
        jackpot.setContributionType("UNKNOWN");

        when(jackpotRepository.findById(1L)).thenReturn(Optional.of(jackpot));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> jackpotService.processBetContribution(bet));
        assertEquals("Unknown contribution strategy: UNKNOWN", exception.getMessage());
    }

    @Test
    void testEvaluateJackpotReward_BetWins_ReturnsRewardAmount() {
        // Given
        Long betId = 1L;

        JackpotContribution contribution = JackpotContribution.builder()
                .betId(betId)
                .userId(100L)
                .jackpotId(1L)
                .stakeAmount(new BigDecimal("100.00"))
                .contributionAmount(new BigDecimal("10.00"))
                .currentJackpotAmount(new BigDecimal("1500.00"))
                .build();

        Jackpot jackpot = new Jackpot();
        jackpot.setId(1L);
        jackpot.setInitialPoolValue(new BigDecimal("1000.00"));
        jackpot.setCurrentPoolValue(new BigDecimal("1500.00"));
        jackpot.setRewardType("FIXED_REWARD");
        jackpot.setRewardConfig("{\"percentage\": 0.05}");

        when(contributionRepository.findByBetId(betId)).thenReturn(List.of(contribution));
        when(jackpotRepository.findById(1L)).thenReturn(Optional.of(jackpot));
        when(fixedRewardStrategy.evaluateWin(
                eq(new BigDecimal("1500.00")),
                eq("{\"percentage\": 0.05}")
        )).thenReturn(true);

        // When
        BigDecimal reward = jackpotService.evaluateJackpotReward(betId);

        // Then
        assertNotNull(reward);
        assertEquals(new BigDecimal("1500.00"), reward);

        // Verify reward was saved
        verify(rewardRepository).save(any());

        // Verify jackpot was reset
        ArgumentCaptor<Jackpot> jackpotCaptor = ArgumentCaptor.forClass(Jackpot.class);
        verify(jackpotRepository).save(jackpotCaptor.capture());
        assertEquals(new BigDecimal("1000.00"), jackpotCaptor.getValue().getCurrentPoolValue());
    }

    @Test
    void testEvaluateJackpotReward_BetDoesNotWin_ReturnsNull() {
        // Given
        Long betId = 2L;

        JackpotContribution contribution = JackpotContribution.builder()
                .betId(betId)
                .userId(200L)
                .jackpotId(2L)
                .build();

        Jackpot jackpot = new Jackpot();
        jackpot.setId(2L);
        jackpot.setCurrentPoolValue(new BigDecimal("5000.00"));
        jackpot.setRewardType("FIXED_REWARD");
        jackpot.setRewardConfig("{\"percentage\": 0.05}");

        when(contributionRepository.findByBetId(betId)).thenReturn(List.of(contribution));
        when(jackpotRepository.findById(2L)).thenReturn(Optional.of(jackpot));
        when(fixedRewardStrategy.evaluateWin(any(), any())).thenReturn(false);

        // When
        BigDecimal reward = jackpotService.evaluateJackpotReward(betId);

        // Then
        assertNull(reward);
        verify(rewardRepository, never()).save(any());
        verify(jackpotRepository, never()).save(any());
    }

    @Test
    void testEvaluateJackpotReward_ContributionNotFound_ThrowsException() {
        // Given
        Long betId = 999L;
        when(contributionRepository.findByBetId(betId)).thenReturn(Collections.emptyList());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> jackpotService.evaluateJackpotReward(betId));
        assertEquals("No contribution found for bet: 999", exception.getMessage());
    }

    @Test
    void testEvaluateJackpotReward_UnknownRewardStrategy_ThrowsException() {
        // Given
        Long betId = 1L;

        JackpotContribution contribution = JackpotContribution.builder()
                .betId(betId)
                .userId(100L)
                .jackpotId(1L)
                .build();

        Jackpot jackpot = new Jackpot();
        jackpot.setId(1L);
        jackpot.setRewardType("UNKNOWN");

        when(contributionRepository.findByBetId(betId)).thenReturn(List.of(contribution));
        when(jackpotRepository.findById(1L)).thenReturn(Optional.of(jackpot));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> jackpotService.evaluateJackpotReward(betId));
        assertEquals("Unknown reward strategy: UNKNOWN", exception.getMessage());
    }

    @Test
    void testGetJackpot_Found_ReturnsJackpot() {
        // Given
        Long jackpotId = 1L;
        Jackpot jackpot = new Jackpot();
        jackpot.setId(jackpotId);
        jackpot.setInitialPoolValue(new BigDecimal("1000.00"));

        when(jackpotRepository.findById(jackpotId)).thenReturn(Optional.of(jackpot));

        // When
        Jackpot result = jackpotService.getJackpot(jackpotId);

        // Then
        assertNotNull(result);
        assertEquals(jackpotId, result.getId());
        assertEquals(new BigDecimal("1000.00"), result.getInitialPoolValue());
    }

    @Test
    void testGetJackpot_NotFound_ThrowsException() {
        // Given
        Long jackpotId = 999L;
        when(jackpotRepository.findById(jackpotId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> jackpotService.getJackpot(jackpotId));
        assertEquals("Jackpot not found: 999", exception.getMessage());
    }
}
