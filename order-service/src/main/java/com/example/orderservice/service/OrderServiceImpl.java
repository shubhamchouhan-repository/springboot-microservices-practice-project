package com.example.orderservice.service;

import com.example.orderservice.client.UserClient;
import com.example.orderservice.dto.OrderRequest;
import com.example.orderservice.dto.OrderResponse;
import com.example.orderservice.dto.UserDto;
import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderStatus;
import com.example.orderservice.event.OrderPlacedEvent;
import com.example.orderservice.exception.OrderNotFoundException;
import com.example.orderservice.exception.ServiceUnavailableException;
import com.example.orderservice.producer.OrderEventProducer;
import com.example.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.util.List;

/** Business logic for orders, including calling USER-SERVICE and computing totalAmount. */
@Slf4j
@Service

public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserClient userClient;

    private final OrderEventProducer orderEventProducer;

    public OrderServiceImpl(OrderRepository orderRepository, UserClient userClient,  OrderEventProducer orderEventProducer) {
        this.orderRepository = orderRepository;
        this.userClient = userClient;
        this.orderEventProducer = orderEventProducer;
    }



//    @Override
//    @Transactional
//    public OrderResponse createOrder(OrderRequest request) {
//        log.info("Creating order for userId={}", request.getUserId());
//
////        userClient.validateUserExists(request.getUserId()); // fails fast if the user is missing\
//
//        // TOPIC NAME: WebClient -> Flux
//        userClient.validateUserExists(request.getUserId()).block(); // fails fast if the user is missing
//        // createOrder() returns OrderResponse rather than Mono<OrderResponse>, you can block for the validation result before saving the order
//
//
//
//        Order order = Order.builder()
//                .userId(request.getUserId())
//                .productName(request.getProductName())
//                .quantity(request.getQuantity())
//                .price(request.getPrice())
//                .totalAmount(calculateTotal(request))
//                .orderStatus(request.getOrderStatus() != null ? request.getOrderStatus() : OrderStatus.PENDING)
//                .build();
//        Order saved = orderRepository.save(order);
//        log.info("Order created id={} total={}", saved.getId(), saved.getTotalAmount());
//        return toResponse(saved);
//    }


// TOPIC NAME: KAFKA
@Override
@Transactional
public OrderResponse createOrder(OrderRequest request) {

    log.info("Creating order for userId={}", request.getUserId());

    // Step 1: Validate that the user exists in user-service.
    // block() waits for the WebClient response before continuing.
    userClient.validateUserExists(request.getUserId()).block();

    // Step 2: Build the order entity.
    Order order = Order.builder()
            .userId(request.getUserId())
            .productName(request.getProductName())
            .quantity(request.getQuantity())
            .price(request.getPrice())
            .totalAmount(calculateTotal(request))
            .orderStatus(
                    request.getOrderStatus() != null
                            ? request.getOrderStatus()
                            : OrderStatus.PENDING
            )
            .build();

    // Step 3: Save the order to the database.
    Order saved = orderRepository.save(order);

    log.info(
            "Order created id={} total={}",
            saved.getId(),
            saved.getTotalAmount()
    );

    // Step 4: Publish an event to Kafka after saving the order.
    orderEventProducer.publishOrderPlacedEvent(
            new OrderPlacedEvent(
                    saved.getId(),
                    saved.getUserId(),
                    saved.getTotalAmount().doubleValue(),
                    saved.getOrderStatus().name()
            )
    );

    // Step 5: Return the order response to the caller.
    return toResponse(saved);
}
/*
    Order Service
Validate user through WebClient
        |
    PostgreSQL
Save order and generate order ID
        |
    Kafka — order-placed topic
Publish OrderPlacedEvent
        |
    Notification Service
Consume the event and log or send the notification

*/


    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        log.info("Fetching all orders");
        return orderRepository.findAll().stream().map(this::toResponse).toList();
    }


    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        log.info("Fetching order id={}", id);
        return toResponse(findOrThrow(id));
    }


    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserId(Long userId) {
        log.info("Fetching orders for userId={}", userId);
        return orderRepository.findByUserId(userId).stream().map(this::toResponse).toList();
    }


    @Override
    @Transactional
    public OrderResponse updateOrder(Long id, OrderRequest request) {
        log.info("Updating order id={}", id);
        Order order = findOrThrow(id);
        // Only call USER-SERVICE again if the order is being moved to a different user
        if (!order.getUserId().equals(request.getUserId())) {
            userClient.validateUserExists(request.getUserId());
        }
        order.setUserId(request.getUserId());
        order.setProductName(request.getProductName());
        order.setQuantity(request.getQuantity());
        order.setPrice(request.getPrice());
        order.setTotalAmount(calculateTotal(request));
        if (request.getOrderStatus() != null) {
            order.setOrderStatus(request.getOrderStatus());
        }
        return toResponse(orderRepository.save(order));
    }


    @Override
    @Transactional
    public void deleteOrder(Long id) {
        log.info("Deleting order id={}", id);
        orderRepository.delete(findOrThrow(id));
    }

    // WebClient with Flux : one request , many users back
    @GetMapping("/users")
    public Flux<UserDto> getAllUsers() {
        return userClient.getAllUsers();
    }


    /** totalAmount = quantity x price, always computed on the server. */
    private BigDecimal calculateTotal(OrderRequest r) {
        return r.getPrice().multiply(BigDecimal.valueOf(r.getQuantity()));
    }


    private Order findOrThrow(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> {
            log.warn("Order not found id={}", id);
            return new OrderNotFoundException("Order not found with id " + id);
        });
    }

    private OrderResponse toResponse(Order o) {
        return OrderResponse.builder()
                .id(o.getId())
                .userId(o.getUserId())
                .productName(o.getProductName())
                .quantity(o.getQuantity())
                .price(o.getPrice())
                .totalAmount(o.getTotalAmount())
                .orderStatus(o.getOrderStatus())
                .createdAt(o.getCreatedAt())
                .build();
    }
}
