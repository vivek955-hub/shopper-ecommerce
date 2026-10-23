package com.example.ecom_project.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.example.ecom_project.model.Product;

/**
 * Product as sent to clients. It deliberately has no image bytes:
 * images are served separately from /api/product/{id}/image.
 * Wrapper types are used so the JPQL constructor expressions in ProductRepo match exactly.
 */
public record ProductResponse(
        Integer id,
        String name,
        String description,
        String brand,
        BigDecimal price,
        String category,
        LocalDate releaseDate,
        Boolean productAvailable,
        Integer stockQuantity,
        String imageName,
        String imageType) {

    public static ProductResponse from(Product p) {
        return new ProductResponse(
                p.getId(), p.getName(), p.getDescription(), p.getBrand(), p.getPrice(), p.getCategory(),
                p.getReleaseDate(), p.isProductAvailable(), p.getStockQuantity(), p.getImageName(), p.getImageType());
    }
}
