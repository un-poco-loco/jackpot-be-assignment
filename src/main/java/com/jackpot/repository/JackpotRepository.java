package com.jackpot.repository;

import com.jackpot.model.Jackpot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Jackpot entity operations.
 */
@Repository
public interface JackpotRepository extends JpaRepository<Jackpot, Long> {
}
