package com.example.ecom_project.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.example.ecom_project.model.CustomerOrder;

public record OrderResponse(
        Long id,
        Instant createdAt,
        String status,
        BigDecimal totalAmount,
        List<OrderItemResponse> items) {

    public static OrderResponse from(CustomerOrder order) {
        List<OrderItemResponse> items = order.getItems().stream().map(OrderItemResponse::from).toList();
        return new OrderResponse(
                order.getId(), order.getCreatedAt(), order.getStatus().name(), order.getTotalAmount(), items);
    }
}
