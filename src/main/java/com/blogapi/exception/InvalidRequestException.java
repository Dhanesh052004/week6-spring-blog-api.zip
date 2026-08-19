package com.blogapi.exception;

/**
 * Thrown when a request is well-formed but semantically invalid
 * (e.g. referencing a category that does not exist, duplicate name, etc.).
 */
public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }
}
