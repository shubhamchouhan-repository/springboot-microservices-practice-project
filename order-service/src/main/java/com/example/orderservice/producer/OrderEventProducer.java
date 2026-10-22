package com.example.orderservice.producer;


import com.example.orderservice.event.OrderPlacedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderEventProducer {

    private static final String TOPIC = "order-placed";

    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    public OrderEventProducer(
            KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

//    public void  publishOrderPlacedEvent(OrderPlacedEvent event) {
//
//        // Use the order ID as the message key.
//        // Events for the same order key go to the same partition.
//        kafkaTemplate.send(
//                TOPIC,
//                event.orderId().toString(),
//                event
//        );
//    }

    public void publishOrderPlacedEvent(OrderPlacedEvent event) {

        String key = event.orderId().toString();

        log.info("Publishing order event: orderId={}, topic={}", event.orderId(), TOPIC);

        kafkaTemplate.send(TOPIC, key, event)
                .whenComplete((result, exception) -> {
                    if (exception != null) {
                        log.error("Failed to publish order event: orderId={}",
                                event.orderId(), exception);
                    } else {
                        log.info(
                                "Order event published successfully: orderId={}, partition={}, offset={}",
                                event.orderId(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset()
                        );
                    }
                });
    }
}
