package com.example.orderservice.exception;

/** Thrown when USER-SERVICE cannot be reached -> HTTP 503. */
public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(String message) {
        super(message);
    }
}
