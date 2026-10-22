package com.example.orderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/** Entry point of Order Service. */
/**
 * Entry point of Order Service.
 * @EnableFeignClients tells Spring to scan for interfaces marked with @FeignClient
 * and create working HTTP clients for them automatically.
 */
@SpringBootApplication
@EnableFeignClients
public class OrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
