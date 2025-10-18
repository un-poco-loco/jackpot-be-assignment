package com.jackpot.repository;

import com.jackpot.model.JackpotContribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for JackpotContribution entity operations.
 */
@Repository
public interface ContributionRepository extends JpaRepository<JackpotContribution, Long> {

    /**
     * Find all contributions for a specific jackpot.
     *
     * @param jackpotId the jackpot ID
     * @return list of contributions
     */
    List<JackpotContribution> findByJackpotId(Long jackpotId);

    /**
     * Find contribution by bet ID.
     *
     * @param betId the bet ID
     * @return list of contributions (should be one)
     */
    List<JackpotContribution> findByBetId(Long betId);
}
