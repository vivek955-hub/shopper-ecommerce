package com.example.ecom_project.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.ecom_project.dto.OrderItemResponse;
import com.example.ecom_project.dto.OrderRequest;
import com.example.ecom_project.dto.OrderResponse;
import com.example.ecom_project.exception.InsufficientStockException;
import com.example.ecom_project.exception.ResourceNotFoundException;
import com.example.ecom_project.service.OrderService;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService service;

    private static OrderResponse sampleOrder() {
        OrderItemResponse item = new OrderItemResponse(1, "Phone", new BigDecimal("500.00"), 2, new BigDecimal("1000.00"));
        return new OrderResponse(10L, Instant.parse("2025-01-01T10:00:00Z"), "PLACED", new BigDecimal("1000.00"), List.of(item));
    }

    @Test
    void placeOrderReturns201WithOrderDetails() throws Exception {
        when(service.placeOrder(any(OrderRequest.class))).thenReturn(sampleOrder());

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"productId\":1,\"quantity\":2}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("PLACED"))
                .andExpect(jsonPath("$.totalAmount").value(1000.00))
                .andExpect(jsonPath("$.items[0].productName").value("Phone"));
    }

    @Test
    void emptyOrderIsRejectedWith400() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.items").exists());
    }

    @Test
    void invalidQuantityIsRejectedWith400() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"productId\":1,\"quantity\":0}]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void malformedJsonIsRejectedWith400() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void notEnoughStockReturns409WithMessage() throws Exception {
        when(service.placeOrder(any(OrderRequest.class)))
                .thenThrow(new InsufficientStockException("Only 1 of 'Phone' in stock (requested 5)"));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"productId\":1,\"quantity\":5}]}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Only 1 of 'Phone' in stock (requested 5)"));
    }

    @Test
    void unknownOrderReturns404() throws Exception {
        when(service.getOrder(99L)).thenThrow(new ResourceNotFoundException("Order 99 not found"));

        mockMvc.perform(get("/api/orders/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Order 99 not found"));
    }
}
