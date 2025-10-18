package com.jackpot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for jackpot evaluation responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response object for jackpot evaluation results")
public class JackpotEvaluationResponse {

    @Schema(description = "Indicates whether the bet won the jackpot", example = "true")
    private boolean won;

    @Schema(description = "The reward amount if won, zero otherwise", example = "5000.00")
    private BigDecimal rewardAmount;

    @Schema(description = "Human-readable message about the evaluation result",
            example = "Congratulations! You won the jackpot!")
    private String message;
}
