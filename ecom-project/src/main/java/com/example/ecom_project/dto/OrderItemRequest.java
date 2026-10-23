package com.example.ecom_project.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemRequest(
        @NotNull(message = "productId is required")
        Integer productId,

        @NotNull(message = "quantity is required")
        @Positive(message = "quantity must be at least 1")
        @Max(value = 100, message = "quantity per item cannot exceed 100")
        Integer quantity) {
}
