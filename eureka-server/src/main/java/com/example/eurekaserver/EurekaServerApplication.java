package com.example.eurekaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Eureka Server = the "phone book" of our system.
 * Every microservice registers its name + address here, and others look it up.
 */
@SpringBootApplication
@EnableEurekaServer // turns this app into a service registry + dashboard
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
