package com.github.karmadeb.closedblocks.api.exceptions;

/**
 * This exception gets thrown when a
 * world was expected but the world instance
 * is not valid
 */
public class InvalidWorldException extends RuntimeException {

    /**
     * Create the exception
     *
     * @param message the exception message
     */
    public InvalidWorldException(final String message) {
        super(message);
    }
}