package com.example.orderservice.exception;

/** Thrown when USER-SERVICE says the user does not exist -> HTTP 404. */
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
