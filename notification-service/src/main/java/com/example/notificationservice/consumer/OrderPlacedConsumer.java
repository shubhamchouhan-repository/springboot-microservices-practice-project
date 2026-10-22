package com.example.notificationservice.consumer;


import com.example.notificationservice.event.OrderPlacedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderPlacedConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderPlacedConsumer.class);

    // Spring Kafka invokes this method when an event
    // arrives in the order-placed topic.
    @KafkaListener(topics = "order-placed")
    public void handleOrderPlaced(OrderPlacedEvent event) {

        log.info("Received order placed event: {}", event);

        // For the first implementation, simulate a notification.
        // Later, replace this with email/SMS delivery.
        log.info(
                "NOTIFICATION: Order {} placed successfully. " +
                        "User ID: {}, Amount: {}, Status: {}",
                event.orderId(),
                event.userId(),
                event.totalAmount(),
                event.status()
        );
    }
}
