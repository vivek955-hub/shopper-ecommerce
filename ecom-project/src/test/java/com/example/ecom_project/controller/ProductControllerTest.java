package com.example.ecom_project.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.ecom_project.dto.PageResponse;
import com.example.ecom_project.dto.ProductResponse;
import com.example.ecom_project.exception.ResourceNotFoundException;
import com.example.ecom_project.service.ProductService;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService service;

    private static ProductResponse phone() {
        return new ProductResponse(1, "Phone", "A phone", "Acme", new BigDecimal("199.99"), "Mobile",
                LocalDate.of(2024, 1, 1), true, 10, "phone.png", "image/png");
    }

    private static MockMultipartFile productPart(String json) {
        return new MockMultipartFile("product", "", MediaType.APPLICATION_JSON_VALUE,
                json.getBytes(StandardCharsets.UTF_8));
    }

    private static final String VALID_PRODUCT_JSON =
            "{\"name\":\"Phone\",\"description\":\"A phone\",\"brand\":\"Acme\",\"price\":199.99,"
                    + "\"category\":\"Mobile\",\"releaseDate\":\"2024-01-01\",\"productAvailable\":true,"
                    + "\"stockQuantity\":10}";

    @Test
    void listReturnsPagedProducts() throws Exception {
        PageResponse<ProductResponse> page = new PageResponse<>(List.of(phone()), 0, 12, 1, 1);
        when(service.getProducts(any(), any(), anyInt(), anyInt())).thenReturn(page);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Phone"))
                .andExpect(jsonPath("$.content[0].imageData").doesNotExist())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void getUnknownProductReturnsJson404() throws Exception {
        when(service.getById(99)).thenThrow(new ResourceNotFoundException("Product 99 not found"));

        mockMvc.perform(get("/api/product/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Product 99 not found"));
    }

    @Test
    void nonNumericIdReturns400NotServerError() throws Exception {
        mockMvc.perform(get("/api/product/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createWithValidDataReturns201() throws Exception {
        when(service.create(any(), any())).thenReturn(phone());
        MockMultipartFile image = new MockMultipartFile("imageFile", "phone.png", "image/png", new byte[] {1, 2});

        mockMvc.perform(multipart("/api/product").file(productPart(VALID_PRODUCT_JSON)).file(image))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Phone"));
    }

    @Test
    void createWithoutImageIsAllowed() throws Exception {
        when(service.create(any(), any())).thenReturn(phone());

        mockMvc.perform(multipart("/api/product").file(productPart(VALID_PRODUCT_JSON)))
                .andExpect(status().isCreated());
    }

    @Test
    void createWithInvalidDataReturns400WithFieldErrors() throws Exception {
        String invalid = "{\"name\":\"\",\"brand\":\"Acme\",\"price\":-5,\"category\":\"Mobile\",\"stockQuantity\":-1}";

        mockMvc.perform(multipart("/api/product").file(productPart(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.price").exists())
                .andExpect(jsonPath("$.errors.stockQuantity").exists());
    }

    @Test
    void updateReturns200() throws Exception {
        when(service.update(anyInt(), any(), any())).thenReturn(phone());

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/product/1").file(productPart(VALID_PRODUCT_JSON)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Phone"));
    }

    @Test
    void deleteReturns204() throws Exception {
        mockMvc.perform(delete("/api/product/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUnknownProductReturns404() throws Exception {
        doThrow(new ResourceNotFoundException("Product 5 not found")).when(service).delete(5);

        mockMvc.perform(delete("/api/product/5"))
                .andExpect(status().isNotFound());
    }

    @Test
    void wrongHttpMethodReturns405NotServerError() throws Exception {
        mockMvc.perform(delete("/api/products"))
                .andExpect(status().isMethodNotAllowed());
    }
}
