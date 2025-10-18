package com.jackpot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a jackpot reward won by a user.
 */
@Entity
@Table(name = "jackpot_reward")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JackpotReward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bet_id", nullable = false)
    private Long betId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "jackpot_id", nullable = false)
    private Long jackpotId;

    @Column(name = "jackpot_reward_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal jackpotRewardAmount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
