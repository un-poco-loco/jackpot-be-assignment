package com.jackpot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Represents a bet placed by a user.
 * This is the message structure sent through Kafka.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bet implements Serializable {

    private Long betId;
    private Long userId;
    private Long jackpotId;
    private BigDecimal betAmount;
}
