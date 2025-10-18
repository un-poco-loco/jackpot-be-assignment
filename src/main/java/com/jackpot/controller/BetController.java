package com.jackpot.controller;

import com.jackpot.dto.BetRequest;
import com.jackpot.model.Bet;
import com.jackpot.service.BetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for bet operations.
 */
@RestController
@RequestMapping("/api/bets")
@Tag(name = "Bet Management", description = "APIs for publishing and managing bets")
public class BetController {

    private static final Logger logger = LoggerFactory.getLogger(BetController.class);

    private final BetService betService;

    public BetController(BetService betService) {
        this.betService = betService;
    }

    /**
     * Publishes a bet to the Kafka topic for processing.
     *
     * @param betRequest the bet request
     * @return response with status
     */
    @Operation(
            summary = "Submit a bet",
            description = "Publishes a bet to the Kafka topic for jackpot contribution processing. " +
                    "The bet will be processed asynchronously and contributions will be calculated " +
                    "based on the jackpot's contribution strategy."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "202",
                    description = "Bet accepted for processing",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "Bet published successfully")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "Error publishing bet: <error message>")
                    )
            )
    })
    @PostMapping
    public ResponseEntity<String> publishBet(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Bet details to be submitted",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BetRequest.class),
                            examples = @ExampleObject(
                                    name = "Sample Bet",
                                    value = "{\"betId\": 123, \"userId\": 456, \"jackpotId\": 1, \"betAmount\": 100.00}"
                            )
                    )
            )
            @Valid @RequestBody BetRequest betRequest) {
        try {
            logger.info("Received bet request - BetId: {}, UserId: {}, JackpotId: {}, Amount: {}",
                    betRequest.getBetId(), betRequest.getUserId(),
                    betRequest.getJackpotId(), betRequest.getBetAmount());

            Bet bet = new Bet(
                    betRequest.getBetId(),
                    betRequest.getUserId(),
                    betRequest.getJackpotId(),
                    betRequest.getBetAmount()
            );

            betService.publishBet(bet);

            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body("Bet published successfully");
        } catch (Exception e) {
            logger.error("Error publishing bet", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error publishing bet: " + e.getMessage());
        }
    }
}
