package com.example.task2.exception;

/**
 * Exception thrown when a requested album is not found.
 */
public class AlbumNotFoundException extends RuntimeException {

    /**
     * Constructs a new AlbumNotFoundException with the specified detail message.
     *
     * @param message the detail message
     */
    public AlbumNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new AlbumNotFoundException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public AlbumNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
