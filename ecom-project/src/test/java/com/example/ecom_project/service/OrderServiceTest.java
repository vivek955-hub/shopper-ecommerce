package com.example.ecom_project.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.ecom_project.dto.OrderItemRequest;
import com.example.ecom_project.dto.OrderRequest;
import com.example.ecom_project.dto.OrderResponse;
import com.example.ecom_project.dto.ProductResponse;
import com.example.ecom_project.exception.InsufficientStockException;
import com.example.ecom_project.exception.ResourceNotFoundException;
import com.example.ecom_project.model.CustomerOrder;
import com.example.ecom_project.repo.OrderRepo;
import com.example.ecom_project.repo.ProductRepo;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private ProductRepo productRepo;

    @Mock
    private OrderRepo orderRepo;

    @InjectMocks
    private OrderService orderService;

    private static ProductResponse product(int id, String name, String price, int stock, boolean available) {
        return new ProductResponse(id, name, "desc", "Brand", new BigDecimal(price), "Mobile",
                LocalDate.of(2024, 1, 1), available, stock, null, null);
    }

    private static OrderRequest request(OrderItemRequest... items) {
        return new OrderRequest(List.of(items));
    }

    @Test
    void placeOrderReservesStockAndCalculatesTotalFromDatabasePrices() {
        when(productRepo.findSummaryById(1)).thenReturn(Optional.of(product(1, "Phone", "500.00", 10, true)));
        when(productRepo.findSummaryById(2)).thenReturn(Optional.of(product(2, "Case", "12.50", 40, true)));
        when(productRepo.decrementStock(1, 2)).thenReturn(1);
        when(productRepo.decrementStock(2, 1)).thenReturn(1);
        when(orderRepo.save(any(CustomerOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = orderService.placeOrder(
                request(new OrderItemRequest(1, 2), new OrderItemRequest(2, 1)));

        // 2 x 500.00 + 1 x 12.50
        assertEquals(0, new BigDecimal("1012.50").compareTo(response.totalAmount()));
        assertEquals("PLACED", response.status());
        assertEquals(2, response.items().size());
        assertEquals("Phone", response.items().get(0).productName());
        verify(productRepo).markUnavailableIfOutOfStock(1);
        verify(productRepo).markUnavailableIfOutOfStock(2);
    }

    @Test
    void duplicateLinesForTheSameProductAreMerged() {
        when(productRepo.findSummaryById(1)).thenReturn(Optional.of(product(1, "Phone", "100.00", 10, true)));
        when(productRepo.decrementStock(1, 5)).thenReturn(1);
        when(orderRepo.save(any(CustomerOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = orderService.placeOrder(
                request(new OrderItemRequest(1, 2), new OrderItemRequest(1, 3)));

        assertEquals(1, response.items().size());
        assertEquals(5, response.items().get(0).quantity());
        verify(productRepo, times(1)).decrementStock(1, 5);
    }

    @Test
    void notEnoughStockIsRejectedAndNothingIsSaved() {
        when(productRepo.findSummaryById(1)).thenReturn(Optional.of(product(1, "Phone", "500.00", 1, true)));
        when(productRepo.decrementStock(1, 5)).thenReturn(0);

        InsufficientStockException ex = assertThrows(InsufficientStockException.class,
                () -> orderService.placeOrder(request(new OrderItemRequest(1, 5))));

        assertTrue(ex.getMessage().contains("Only 1"));
        verify(orderRepo, never()).save(any(CustomerOrder.class));
    }

    @Test
    void unavailableProductIsRejectedWithClearMessage() {
        when(productRepo.findSummaryById(1)).thenReturn(Optional.of(product(1, "Phone", "500.00", 10, false)));
        when(productRepo.decrementStock(1, 1)).thenReturn(0);

        InsufficientStockException ex = assertThrows(InsufficientStockException.class,
                () -> orderService.placeOrder(request(new OrderItemRequest(1, 1))));

        assertTrue(ex.getMessage().contains("unavailable"));
    }

    @Test
    void unknownProductIsRejected() {
        when(productRepo.findSummaryById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.placeOrder(request(new OrderItemRequest(99, 1))));

        verify(productRepo, never()).decrementStock(anyInt(), anyInt());
        verify(orderRepo, never()).save(any(CustomerOrder.class));
    }

    @Test
    void getOrderThrowsWhenMissing() {
        when(orderRepo.findById(7L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrder(7L));
    }
}
