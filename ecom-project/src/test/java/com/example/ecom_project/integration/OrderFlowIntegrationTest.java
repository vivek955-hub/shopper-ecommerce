package com.example.ecom_project.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.ecom_project.dto.OrderItemRequest;
import com.example.ecom_project.dto.OrderRequest;
import com.example.ecom_project.exception.InsufficientStockException;
import com.example.ecom_project.model.Product;
import com.example.ecom_project.repo.OrderRepo;
import com.example.ecom_project.repo.ProductRepo;
import com.example.ecom_project.service.OrderService;

/**
 * End-to-end tests of the order flow against a real (in-memory H2) database.
 * No @Transactional on the class on purpose: we want real commits, like production.
 */
@SpringBootTest
@AutoConfigureMockMvc
class OrderFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private OrderService orderService;

    @AfterEach
    void cleanUp() {
        orderRepo.deleteAll();
        productRepo.deleteAll();
    }

    private Product saveProduct(String name, String price, int stock) {
        Product p = new Product();
        p.setName(name);
        p.setBrand("Acme");
        p.setCategory("Mobile");
        p.setPrice(new BigDecimal(price));
        p.setReleaseDate(LocalDate.of(2024, 1, 1));
        p.setProductAvailable(true);
        p.setStockQuantity(stock);
        return productRepo.save(p);
    }

    private static String orderJson(int productId, int quantity) {
        return "{\"items\":[{\"productId\":" + productId + ",\"quantity\":" + quantity + "}]}";
    }

    @Test
    void placingAnOrderReducesStockAndKeepsAPriceSnapshot() throws Exception {
        Product phone = saveProduct("Phone", "250.00", 5);

        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(orderJson(phone.getId(), 2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalAmount").value(500.00))
                .andExpect(jsonPath("$.items[0].productName").value("Phone"));

        assertEquals(3, productRepo.findById(phone.getId()).orElseThrow().getStockQuantity());
        assertEquals(1, orderRepo.count());

        // the order list endpoint returns the stored order with its items
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].items[0].quantity").value(2));
    }

    @Test
    void orderingMoreThanInStockReturns409AndChangesNothing() throws Exception {
        Product phone = saveProduct("Phone", "250.00", 1);

        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(orderJson(phone.getId(), 2)))
                .andExpect(status().isConflict());

        assertEquals(1, productRepo.findById(phone.getId()).orElseThrow().getStockQuantity());
        assertEquals(0, orderRepo.count());
    }

    @Test
    void failedOrderRollsBackStockOfItemsThatWereAlreadyReserved() throws Exception {
        Product plenty = saveProduct("Plenty", "10.00", 10);
        Product scarce = saveProduct("Scarce", "20.00", 1);

        // ids are processed in ascending order, so "plenty" is reserved first and then "scarce" fails
        String json = "{\"items\":[{\"productId\":" + plenty.getId() + ",\"quantity\":3},"
                + "{\"productId\":" + scarce.getId() + ",\"quantity\":5}]}";

        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isConflict());

        assertEquals(10, productRepo.findById(plenty.getId()).orElseThrow().getStockQuantity());
        assertEquals(1, productRepo.findById(scarce.getId()).orElseThrow().getStockQuantity());
        assertEquals(0, orderRepo.count());
    }

    @Test
    void buyingTheLastItemMarksTheProductUnavailable() throws Exception {
        Product last = saveProduct("Last one", "99.00", 1);

        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(orderJson(last.getId(), 1)))
                .andExpect(status().isCreated());

        Product reloaded = productRepo.findById(last.getId()).orElseThrow();
        assertEquals(0, reloaded.getStockQuantity());
        assertEquals(false, reloaded.isProductAvailable());
    }

    @Test
    void concurrentBuyersNeverOversellTheLastUnits() throws Exception {
        int stock = 5;
        int buyers = 12;
        Product phone = saveProduct("Hot item", "50.00", stock);
        OrderRequest request = new OrderRequest(List.of(new OrderItemRequest(phone.getId(), 1)));

        ExecutorService pool = Executors.newFixedThreadPool(buyers);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger succeeded = new AtomicInteger();
        AtomicInteger rejected = new AtomicInteger();
        List<Future<Void>> futures = new ArrayList<>();

        for (int i = 0; i < buyers; i++) {
            Callable<Void> buyer = () -> {
                start.await();
                try {
                    orderService.placeOrder(request);
                    succeeded.incrementAndGet();
                } catch (InsufficientStockException e) {
                    rejected.incrementAndGet();
                }
                return null;
            };
            futures.add(pool.submit(buyer));
        }
        start.countDown();
        for (Future<Void> future : futures) {
            future.get(30, TimeUnit.SECONDS);
        }
        pool.shutdown();

        assertEquals(stock, succeeded.get());
        assertEquals(buyers - stock, rejected.get());
        assertEquals(0, productRepo.findById(phone.getId()).orElseThrow().getStockQuantity());
        assertEquals(stock, orderRepo.count());
    }
}
