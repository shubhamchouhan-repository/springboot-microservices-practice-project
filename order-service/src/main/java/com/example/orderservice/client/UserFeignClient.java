package com.example.orderservice.client;

import com.example.orderservice.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client for USER-SERVICE.
 * We only describe WHAT to call; Spring Cloud OpenFeign writes the HTTP code for us.

 * name = "USER-SERVICE" is the Eureka service name (no host or port).
 * Feign asks Eureka where it is and load-balances, just like before.
 */
// TOPIC NAME : OpenFeing


//@FeignClient(name = "USER-SERVICE")
//public interface UserFeignClient {
//
//    //------- Calls GET http://USER-SERVICE/api/users/{id}
//    @GetMapping("/api/users/{id}")
//    UserDto getUserById(@PathVariable("id") Long id);
//}
