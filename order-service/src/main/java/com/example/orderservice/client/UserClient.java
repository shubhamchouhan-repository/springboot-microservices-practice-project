package com.example.orderservice.client;

import com.example.orderservice.dto.UserDto;
import com.example.orderservice.exception.ServiceUnavailableException;
import com.example.orderservice.exception.UserNotFoundException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Talks to USER-SERVICE directly (service-to-service, NOT through the gateway).
 * The host name "USER-SERVICE" is resolved through Eureka - no hardcoded host/port.
 */
//@Slf4j
//@RequiredArgsConstructor
@Component
public class UserClient {


    // TOPIC NAME : RestClient
//    private final RestClient restClient;
//
//    public UserClient(RestClient.Builder loadBalancedBuilder) {
//        this.restClient = loadBalancedBuilder.baseUrl("http://USER-SERVICE").build();
//    }
//
//    /** Returns normally if the user exists; throws otherwise. */
//    public void validateUserExists(Long userId) {
//        log.info("Calling USER-SERVICE to validate userId={}", userId);
//        try {
//            restClient.get()
//                    .uri("/api/users/{id}", userId)
//                    .retrieve()
//                    .onStatus(status -> status.value() == 404, (req, res) -> {
//                        throw new UserNotFoundException("User not found with id " + userId);
//                    })
//                    .onStatus(status -> status.is5xxServerError(), (req, res) -> {
//                        throw new ServiceUnavailableException("User service returned an error");
//                    })
//                    .toBodilessEntity();
//                    // The response body isn't needed because you're only checking whether the user exists. toBodilessEntity() extracts the response status and headers without mapping a response body to a Java object.
//
//        } catch (ResourceAccessException | IllegalStateException ex) {
//            // ResourceAccessException  = connection refused / timeout
//            // IllegalStateException    = load balancer found no instance of USER-SERVICE in Eureka
//            log.error("USER-SERVICE is unreachable: {}", ex.getMessage());
//            throw new ServiceUnavailableException("User service is currently unavailable. Please try again later.");
//        }
//    }


    // TOPIC NAME: OpenFeing
//    private final UserFeignClient userFeignClient;
//
//    /** Returns normally if the user exists; throws otherwise. */
//    public void validateUserExists(Long userId) {
//
//        log.info("Calling USER-SERVICE via Feign to validate userId={}", userId);
//        try {
//            userFeignClient.getUserById(userId);
//        } catch (FeignException.NotFound ex) {
//
//            // USER-SERVICE answered 404 -> the user does not exist
//            log.warn("User not found in USER-SERVICE, userId={}", userId);
//            throw new UserNotFoundException("User not found with id " + userId);
//        } catch (FeignException ex) {
//
//            // Everything else: connection refused, timeout, no instance in Eureka (503), 500 ...
//            log.error("USER-SERVICE call failed, status={}, message={}", ex.status(), ex.getMessage());
//            throw new ServiceUnavailableException("User service is currently unavailable. Please try again later.");
//        }
//    }



    // TOPIC NAME : WebCLient
    private static final Logger log = LoggerFactory.getLogger(UserClient.class);

    private final WebClient webClient;

    public UserClient(WebClient.Builder loadBalancedWebClientBuilder) {
        this.webClient = loadBalancedWebClientBuilder
                // Eureka resolves USER-SERVICE to an available instance.
                .baseUrl("http://USER-SERVICE")
                .build();
    }
    //NOTE: creating web client object



//    /**
//     * Completes normally if the user exists;
//     * emits an error otherwise.
//     */
//    public Mono<Void> validateUserExists(Long userId) {
//
//        log.info("Calling USER-SERVICE to validate userId={}", userId);
//
//        return webClient.get()
//                .uri("/api/users/{id}", userId)
//                .retrieve()
//
//                // User does not exist
//                .onStatus(
//                        status -> status.value() == 404,
//                        response -> Mono.error(
//                                new UserNotFoundException(
//                                        "User not found with id " + userId
//                                )
//                        )
//                )
//
//                // User Service returned a server error
//                .onStatus(
//                        status -> status.is5xxServerError(),
//                        response -> Mono.error(
//                                new ServiceUnavailableException(
//                                        "User service returned an error"
//                                )
//                        )
//                )
//
//                // We only need the HTTP status, not a response body
//                .toBodilessEntity()
//                .then()
//
//                // Handle connection and I/O errors
//                .onErrorMap(
//                        WebClientRequestException.class,
//                        ex -> {
//                            log.error("USER-SERVICE is unreachable: {}", ex.getMessage());
//
//                            return new ServiceUnavailableException("User service is currently unavailable. "+ "Please try again later.");
//                        }
//                );
//        // onStatus() handles HTTP error status codes returned by the server, such as 404 and 500.
//        // onErrorMap() transforms an existing exception into another exception,
//        // for example, converting a connection failure into your custom ServiceUnavailableException.
//    }


    /**
     * Calls USER-SERVICE to check whether a user exists.
     *
     * Retry:
     * Retries temporary service failures.
     *
     * CircuitBreaker:
     * Stops calls when USER-SERVICE is consistently failing.
     *
     * Fallback:
     * Handles exhausted retries or an open circuit.
     */
    @Retry(name = "userService", fallbackMethod = "userServiceFallback")
    @CircuitBreaker(name = "userService", fallbackMethod = "userServiceFallback")
    public Mono<Void> validateUserExists(Long userId) {

        return Mono.defer(() -> {
            // This log should appear for each subscription/attempt.
            log.warn("USER-SERVICE validation attempt for userId={}", userId);

            return webClient.get()
                    .uri("/api/users/{id}", userId)
                    .retrieve()

                    .onStatus(
                            status -> status.value() == 404,
                            response -> Mono.error(
                                    new UserNotFoundException(
                                            "User not found with id " + userId
                                    )
                            )
                    )
                    .onStatus(
                            status -> status.is5xxServerError(),
                            response -> Mono.error(
                                    new ServiceUnavailableException(
                                            "USER-SERVICE returned a server error"
                                    )
                            )
                    )
                    .toBodilessEntity()
                    .then()

                    .onErrorMap(
                            WebClientRequestException.class,
                            ex -> new ServiceUnavailableException(
                                    "USER-SERVICE is currently unreachable"
                            )
                    )

                    // Eureka/LoadBalancer may report no available instances
                    // as an IllegalStateException instead of a network exception.
                    .onErrorMap(
                            ex -> ex instanceof IllegalStateException
                                    && ex.getMessage() != null
                                    && ex.getMessage().contains("USER-SERVICE"),
                            ex -> new ServiceUnavailableException(
                                    "No available USER-SERVICE instance in Eureka"
                            )
                    );
        });
    }

    private Mono<Void> userServiceFallback(Long userId, Throwable ex) {

        log.error(
                "USER-SERVICE FALLBACK EXECUTED for userId={}: {}",
                userId,
                ex.toString()
        );

        // Never treat a failed validation as success.
        return Mono.error(ex instanceof UserNotFoundException
                ? ex
                : new ServiceUnavailableException(
                "USER-SERVICE is unavailable. Please try again later."
        ));
    }



    // TOPIC NAME:  WebClient with Flux : one request , many users back
    public Flux<UserDto> getAllUsers() {

        return webClient.get()
                .uri("/api/users")
                .retrieve()
                .bodyToFlux(UserDto.class)
                .doOnNext(user ->
                        log.info("Received user: {}", user.getId())
                )
                .doOnError(error ->
                        log.error("Failed to fetch users", error)
                );
    }


}


/*

Mono<Void> — represents an asynchronous operation that does not need to return a response body.
Why Mono<Void> instead of Mono<UserResponse>?
Because the Order Service only needs to know whether validation succeeded. It does not need the user's name, email, or other details.

By default, retrieve() turns HTTP 4xx and 5xx responses into errors, unless you customize that behavior.

This converts a successful HTTP response into a Mono<ResponseEntity<Void>>.

.then()
This converts the successful result into a Mono<Void>.

*/
