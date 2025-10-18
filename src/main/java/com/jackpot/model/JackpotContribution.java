package com.jackpot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a contribution made to a jackpot from a bet.
 */
@Entity
@Table(name = "jackpot_contribution")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JackpotContribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bet_id", nullable = false)
    private Long betId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "jackpot_id", nullable = false)
    private Long jackpotId;

    @Column(name = "stake_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal stakeAmount;

    @Column(name = "contribution_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal contributionAmount;

    @Column(name = "current_jackpot_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal currentJackpotAmount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
