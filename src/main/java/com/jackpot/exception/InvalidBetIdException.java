package com.jackpot.exception;

/**
 * Exception thrown when an invalid bet ID is provided.
 */
public class InvalidBetIdException extends RuntimeException {

    public InvalidBetIdException(String message) {
        super(message);
    }

    public InvalidBetIdException(Long betId) {
        super("Invalid bet ID: " + betId + ". Bet ID must be a positive number.");
    }
}
