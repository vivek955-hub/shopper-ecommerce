package com.example.ecom_project.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record OrderRequest(
        @NotEmpty(message = "An order needs at least one item")
        @Valid
        List<OrderItemRequest> items) {
}
