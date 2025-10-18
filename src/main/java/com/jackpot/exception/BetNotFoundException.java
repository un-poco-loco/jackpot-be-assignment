package com.jackpot.exception;

/**
 * Exception thrown when a bet or its contribution cannot be found.
 */
public class BetNotFoundException extends RuntimeException {

    public BetNotFoundException(String message) {
        super(message);
    }

    public BetNotFoundException(Long betId) {
        super("No contribution found for bet ID: " + betId + ". The bet may not have been processed yet or does not exist.");
    }
}
