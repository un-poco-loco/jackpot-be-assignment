package com.jackpot.exception;

/**
 * Exception thrown when a jackpot cannot be found.
 */
public class JackpotNotFoundException extends RuntimeException {

    public JackpotNotFoundException(String message) {
        super(message);
    }

    public JackpotNotFoundException(Long jackpotId) {
        super("Jackpot not found with ID: " + jackpotId);
    }
}
