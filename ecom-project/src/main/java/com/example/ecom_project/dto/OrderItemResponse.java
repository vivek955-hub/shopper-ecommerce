package com.example.ecom_project.dto;

import java.math.BigDecimal;

import com.example.ecom_project.model.OrderItem;

public record OrderItemResponse(
        Integer productId,
        String productName,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal lineTotal) {

    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(
                item.getProductId(), item.getProductName(), item.getUnitPrice(), item.getQuantity(), item.getLineTotal());
    }
}
