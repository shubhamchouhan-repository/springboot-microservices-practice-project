package com.example.notificationservice.event;


// Immutable event describing a successfully placed order.
// Keep the same field names and compatible types
// in the Order Service's event class.
public record OrderPlacedEvent(
        Long orderId,
        Long userId,
        double totalAmount,
        String status
) {
}
