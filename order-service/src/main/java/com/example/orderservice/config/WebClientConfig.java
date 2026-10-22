package com.example.orderservice.config;



import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }
    // NOTE: Create and register the Builder
    // Here, WebClient.builder() creates a Builder object.
    // Spring registers this builder in its IoC container because of @Bean. The @LoadBalanced annotation enables load-balancing integration for this builder.

}







/*

The @LoadBalanced annotation in Spring Boot is used to configure an HTTP client
(like RestTemplate or WebClient) to perform client-side load balancing.


⚙️ How it Works Under the Hood

1. Intercepting Requests: When you mark a RestTemplate bean with @LoadBalanced, Spring Cloud attaches a LoadBalancerInterceptor to it.
2. Service Name Resolution: Instead of passing a hardcoded domain or IP (e.g., http://localhost:8081/api),
you pass the virtual service ID (the value of spring.application.name registered in your service registry like Eureka or Consul):
java
restTemplate.getForObject("http://ORDER-SERVICE/orders", String.class);

3. Instance Selection: The interceptor catches the request, extracts the service name (ORDER-SERVICE),
queries the service registry for all available physical instances (e.g., 10.0.0.5:8081, 10.0.0.6:8082),
 and runs a load-balancing algorithm (like Round-Robin or Random) to pick one.

4. Execution: The interceptor replaces the virtual host (ORDER-SERVICE) with the chosen instance's target IP and port
before sending the actual HTTP request.

*/