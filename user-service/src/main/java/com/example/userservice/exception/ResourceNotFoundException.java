package com.example.userservice.exception;

/** Thrown when a user does not exist -> HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
