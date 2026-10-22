package com.example.userservice.exception;

/** Thrown when the email is already used -> HTTP 409. */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
