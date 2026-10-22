package com.example.orderservice.controller;

import com.example.orderservice.dto.OrderRequest;
import com.example.orderservice.dto.OrderResponse;
import com.example.orderservice.dto.UserDto;
import com.example.orderservice.service.OrderService;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

/** REST endpoints for orders. Thin: delegates everything to OrderService. */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(request));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAll() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }


//    @GetMapping("/{id}")
//    public ResponseEntity<OrderResponse> getById(@PathVariable Long id) {
//        return ResponseEntity.ok(orderService.getOrderById(id));
//    }

    // TOPIC NAME : RATE LIMITER
    @GetMapping("/{id}")
    @RateLimiter(
            name = "orderDetails",
            fallbackMethod = "rateLimitFallback"
    )
    public ResponseEntity<OrderResponse> getById(@PathVariable Long id) {

        // Executes only when the rate limiter grants permission.
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    // Called when the rate limit has been exceeded.
    public ResponseEntity<Map<String, String>> rateLimitFallback(Long id, RequestNotPermitted ex) {

        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS) // HTTP 429
                .header("Retry-After", "10")
                .body(Map.of(
                        "error", "Too many requests, try again later"
                ));
    }


    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponse>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(orderService.getOrdersByUserId(userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponse> update(@PathVariable Long id, @Valid @RequestBody OrderRequest request) {
        return ResponseEntity.ok(orderService.updateOrder(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    // WebClient with Flux : one request , many users back
    @GetMapping("/users")
    public Flux<UserDto> getAllUsers() {
        return orderService.getAllUsers();
    }

}
