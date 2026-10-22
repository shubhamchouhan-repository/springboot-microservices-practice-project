package com.example.orderservice.dto;

import com.example.orderservice.entity.OrderStatus;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/** Request body for create/update. Note: totalAmount is NOT accepted - the backend calculates it. */
@Getter
@Setter
public class OrderRequest {

    @NotNull(message = "userId is required")
    @Positive(message = "userId must be a positive number")
    private Long userId;

    @NotBlank(message = "Product name is required")
    @Size(max = 150, message = "Product name must be at most 150 characters")
    private String productName;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be at least 0.01")
    @Digits(integer = 10, fraction = 2, message = "Price can have at most 2 decimal places")
    private BigDecimal price;

    /** Optional. Defaults to PENDING on create; keeps the current value on update. */
    private OrderStatus orderStatus;
}
