package com.jackpot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a jackpot with configurable contribution and reward strategies.
 */
@Entity
@Table(name = "jackpot")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Jackpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "initial_pool_value", nullable = false, precision = 19, scale = 2)
    private BigDecimal initialPoolValue;

    @Column(name = "current_pool_value", nullable = false, precision = 19, scale = 2)
    private BigDecimal currentPoolValue;

    @Column(name = "contribution_type", nullable = false, length = 50)
    private String contributionType;

    @Column(name = "contribution_config", length = 1000)
    private String contributionConfig;

    @Column(name = "reward_type", nullable = false, length = 50)
    private String rewardType;

    @Column(name = "reward_config", length = 1000)
    private String rewardConfig;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
