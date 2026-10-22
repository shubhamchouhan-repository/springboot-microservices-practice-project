package com.example.userservice.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/** Data we send back to the client. */
@Getter
@Builder
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String phoneNumber;
    private LocalDateTime createdAt;
}
