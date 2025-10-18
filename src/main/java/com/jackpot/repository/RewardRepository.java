package com.jackpot.repository;

import com.jackpot.model.JackpotReward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for JackpotReward entity operations.
 */
@Repository
public interface RewardRepository extends JpaRepository<JackpotReward, Long> {

    /**
     * Find all rewards for a specific jackpot.
     *
     * @param jackpotId the jackpot ID
     * @return list of rewards
     */
    List<JackpotReward> findByJackpotId(Long jackpotId);

    /**
     * Find reward by bet ID.
     *
     * @param betId the bet ID
     * @return list of rewards (should be one if won)
     */
    List<JackpotReward> findByBetId(Long betId);
}
