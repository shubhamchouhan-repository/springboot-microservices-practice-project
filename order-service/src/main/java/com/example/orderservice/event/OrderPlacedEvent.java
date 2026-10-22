package com.example.orderservice.event;


public record OrderPlacedEvent(
        Long orderId,
        Long userId,
        double totalAmount,
        String status
) {
}