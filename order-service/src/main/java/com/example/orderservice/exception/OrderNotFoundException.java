package com.example.orderservice.exception;

/** Thrown when an order id does not exist -> HTTP 404. */
public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String message) {
        super(message);
    }
}
