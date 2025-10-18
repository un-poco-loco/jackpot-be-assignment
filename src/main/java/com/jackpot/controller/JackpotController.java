package com.jackpot.controller;

import com.jackpot.dto.JackpotEvaluationResponse;
import com.jackpot.model.Jackpot;
import com.jackpot.service.JackpotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * REST controller for jackpot operations.
 */
@RestController
@RequestMapping("/api/jackpots")
@Tag(name = "Jackpot Management", description = "APIs for jackpot evaluation and information retrieval")
public class JackpotController {

    private static final Logger logger = LoggerFactory.getLogger(JackpotController.class);

    private final JackpotService jackpotService;

    public JackpotController(JackpotService jackpotService) {
        this.jackpotService = jackpotService;
    }

    /**
     * Evaluates if a bet wins the jackpot.
     *
     * @param betId the bet ID to evaluate
     * @return evaluation response with win status and reward amount
     */
    @Operation(
            summary = "Evaluate jackpot reward for a bet",
            description = "Checks if a bet wins the jackpot based on the configured reward strategy. " +
                    "If the bet wins, the jackpot pool is reset to its initial value and a reward record is created. " +
                    "The evaluation uses either Fixed (random chance) or Variable (pool-based) strategy."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Evaluation completed successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = JackpotEvaluationResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Won Jackpot",
                                            value = "{\"won\": true, \"rewardAmount\": 5000.00, \"message\": \"Congratulations! You won the jackpot!\"}"
                                    ),
                                    @ExampleObject(
                                            name = "Did Not Win",
                                            value = "{\"won\": false, \"rewardAmount\": 0.00, \"message\": \"Better luck next time!\"}"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error during evaluation",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = JackpotEvaluationResponse.class)
                    )
            )
    })
    @GetMapping("/{betId}/evaluate")
    public ResponseEntity<JackpotEvaluationResponse> evaluateJackpot(
            @Parameter(description = "ID of the bet to evaluate", required = true, example = "123")
            @PathVariable Long betId) {
        logger.info("Evaluating jackpot for bet: {}", betId);

        BigDecimal rewardAmount = jackpotService.evaluateJackpotReward(betId);

        JackpotEvaluationResponse response;
        if (rewardAmount != null) {
            response = JackpotEvaluationResponse.builder()
                    .won(true)
                    .rewardAmount(rewardAmount)
                    .message("Congratulations! You won the jackpot!")
                    .build();
            logger.info("Bet {} won jackpot with reward: {}", betId, rewardAmount);
        } else {
            response = JackpotEvaluationResponse.builder()
                    .won(false)
                    .rewardAmount(BigDecimal.ZERO)
                    .message("Better luck next time!")
                    .build();
            logger.info("Bet {} did not win jackpot", betId);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Gets jackpot details by ID.
     *
     * @param jackpotId the jackpot ID
     * @return jackpot details
     */
    @Operation(
            summary = "Get jackpot details",
            description = "Retrieves complete information about a specific jackpot including current pool value, " +
                    "contribution strategy, reward strategy, and configuration details."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Jackpot found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Jackpot.class),
                            examples = @ExampleObject(
                                    value = "{\"id\": 1, \"initialPoolValue\": 1000.00, \"currentPoolValue\": 1200.00, " +
                                            "\"contributionType\": \"FIXED\", \"contributionConfig\": \"{\\\"percentage\\\": 0.10}\", " +
                                            "\"rewardType\": \"FIXED\", \"rewardConfig\": \"{\\\"percentage\\\": 0.05}\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Jackpot not found"
            )
    })
    @GetMapping("/{jackpotId}")
    public ResponseEntity<Jackpot> getJackpot(
            @Parameter(description = "ID of the jackpot to retrieve", required = true, example = "1")
            @PathVariable Long jackpotId) {
        logger.info("Fetching jackpot: {}", jackpotId);
        Jackpot jackpot = jackpotService.getJackpot(jackpotId);
        return ResponseEntity.ok(jackpot);
    }
}
