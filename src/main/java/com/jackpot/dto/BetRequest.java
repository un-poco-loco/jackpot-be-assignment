package com.jackpot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for bet submission requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for submitting a bet")
public class BetRequest {

    @NotNull(message = "Bet ID is required")
    @Positive(message = "Bet ID must be positive")
    @Schema(description = "Unique identifier for the bet", example = "123", required = true)
    private Long betId;

    @NotNull(message = "User ID is required")
    @Positive(message = "User ID must be positive")
    @Schema(description = "Unique identifier for the user placing the bet", example = "456", required = true)
    private Long userId;

    @NotNull(message = "Jackpot ID is required")
    @Positive(message = "Jackpot ID must be positive")
    @Schema(description = "Unique identifier for the jackpot", example = "1", required = true)
    private Long jackpotId;

    @NotNull(message = "Bet amount is required")
    @DecimalMin(value = "0.01", message = "Bet amount must be at least 0.01")
    @Schema(description = "Amount of the bet", example = "100.00", required = true)
    private BigDecimal betAmount;
}
