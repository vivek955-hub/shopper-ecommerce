package com.example.ecom_project.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ecom_project.dto.OrderItemRequest;
import com.example.ecom_project.dto.OrderRequest;
import com.example.ecom_project.dto.OrderResponse;
import com.example.ecom_project.dto.PageResponse;
import com.example.ecom_project.dto.ProductResponse;
import com.example.ecom_project.exception.InsufficientStockException;
import com.example.ecom_project.exception.ResourceNotFoundException;
import com.example.ecom_project.model.CustomerOrder;
import com.example.ecom_project.model.OrderItem;
import com.example.ecom_project.model.OrderStatus;
import com.example.ecom_project.repo.OrderRepo;
import com.example.ecom_project.repo.ProductRepo;

@Service
public class OrderService {

    private final ProductRepo productRepo;
    private final OrderRepo orderRepo;

    public OrderService(ProductRepo productRepo, OrderRepo orderRepo) {
        this.productRepo = productRepo;
        this.orderRepo = orderRepo;
    }

    /**
     * Places an order in a single transaction: either every item is reserved and the
     * order is saved, or nothing changes. Prices always come from the database, never
     * from the client.
     */
    @Transactional
    public OrderResponse placeOrder(OrderRequest request) {
        // Merge duplicate lines. A TreeMap also gives a stable product-id order, so
        // concurrent orders always take row locks in the same sequence (no deadlocks).
        Map<Integer, Integer> quantities = new TreeMap<>();
        for (OrderItemRequest item : request.items()) {
            quantities.merge(item.productId(), item.quantity(), Integer::sum);
        }

        CustomerOrder order = new CustomerOrder();
        order.setCreatedAt(Instant.now());
        order.setStatus(OrderStatus.PLACED);

        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Integer, Integer> entry : quantities.entrySet()) {
            int productId = entry.getKey();
            int quantity = entry.getValue();

            ProductResponse product = productRepo.findSummaryById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product " + productId + " not found"));

            if (productRepo.decrementStock(productId, quantity) == 0) {
                throw notEnoughStock(product, quantity);
            }
            productRepo.markUnavailableIfOutOfStock(productId);

            BigDecimal lineTotal = product.price().multiply(BigDecimal.valueOf(quantity));
            OrderItem line = new OrderItem();
            line.setProductId(productId);
            line.setProductName(product.name());
            line.setUnitPrice(product.price());
            line.setQuantity(quantity);
            line.setLineTotal(lineTotal);
            order.addItem(line);

            total = total.add(lineTotal);
        }
        order.setTotalAmount(total);

        return OrderResponse.from(orderRepo.save(order));
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(long id) {
        return orderRepo.findById(id)
                .map(OrderResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Order " + id + " not found"));
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> listOrders(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);
        PageRequest pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "id"));
        return PageResponse.from(orderRepo.findAll(pageable).map(OrderResponse::from));
    }

    private InsufficientStockException notEnoughStock(ProductResponse product, int requested) {
        if (!Boolean.TRUE.equals(product.productAvailable())) {
            return new InsufficientStockException("'" + product.name() + "' is currently unavailable");
        }
        return new InsufficientStockException("Only " + product.stockQuantity() + " of '" + product.name()
                + "' in stock (requested " + requested + ")");
    }
}
