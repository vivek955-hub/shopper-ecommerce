package com.example.ecom_project.controller;

import java.time.Duration;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.ecom_project.dto.ImageData;
import com.example.ecom_project.dto.PageResponse;
import com.example.ecom_project.dto.ProductRequest;
import com.example.ecom_project.dto.ProductResponse;
import com.example.ecom_project.service.ProductService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    /** Paged list. Optional filters: keyword (searches name/description/brand/category) and category. */
    @GetMapping("/products")
    public PageResponse<ProductResponse> getProducts(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return service.getProducts(keyword, category, page, size);
    }

    @GetMapping("/product/{id}")
    public ProductResponse getProduct(@PathVariable int id) {
        return service.getById(id);
    }

    @PostMapping("/product")
    public ResponseEntity<ProductResponse> addProduct(
            @RequestPart("product") @Valid ProductRequest product,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) {
        ProductResponse created = service.create(product, imageFile);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/product/{id}")
    public ProductResponse updateProduct(
            @PathVariable int id,
            @RequestPart("product") @Valid ProductRequest product,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) {
        return service.update(id, product, imageFile);
    }

    @DeleteMapping("/product/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable int id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/product/{id}/image")
    public ResponseEntity<byte[]> getImage(@PathVariable int id) {
        ImageData image = service.getImage(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.contentType()))
                .cacheControl(CacheControl.maxAge(Duration.ofSeconds(60)).cachePublic())
                .body(image.data());
    }
}
