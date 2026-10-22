package com.example.orderservice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Shape of the user data we get back from USER-SERVICE.
 * We only list the fields Order Service cares about; any extra fields
 * in the JSON (like phoneNumber or createdAt) are simply ignored.
 */
@Getter
@Setter
@NoArgsConstructor
public class UserDto {

    private Long id;
    private String name;
    private String email;

}
