package com.example.ecom_project.service;

import java.io.IOException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.ecom_project.dto.ImageData;
import com.example.ecom_project.dto.PageResponse;
import com.example.ecom_project.dto.ProductRequest;
import com.example.ecom_project.dto.ProductResponse;
import com.example.ecom_project.exception.BadRequestException;
import com.example.ecom_project.exception.ResourceNotFoundException;
import com.example.ecom_project.model.Product;
import com.example.ecom_project.repo.ProductRepo;

@Service
public class ProductService {

    static final int MAX_PAGE_SIZE = 50;

    private final ProductRepo repo;

    public ProductService(ProductRepo repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getProducts(String keyword, String category, int page, int size) {
        String safeKeyword = keyword == null ? "" : keyword.trim();
        String safeCategory = category == null ? "" : category.trim();
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "id"));
        Page<ProductResponse> result = repo.search(safeKeyword, safeCategory, pageable);
        return PageResponse.from(result);
    }

    @Transactional(readOnly = true)
    public ProductResponse getById(int id) {
        return repo.findSummaryById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product " + id + " not found"));
    }

    @Transactional
    public ProductResponse create(ProductRequest request, MultipartFile image) {
        Product product = new Product();
        applyFields(product, request);
        applyImage(product, image);
        return ProductResponse.from(repo.save(product));
    }

    @Transactional
    public ProductResponse update(int id, ProductRequest request, MultipartFile image) {
        Product product = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product " + id + " not found"));
        applyFields(product, request);
        applyImage(product, image); // keeps the existing image when none is uploaded
        return ProductResponse.from(repo.save(product));
    }

    @Transactional
    public void delete(int id) {
        if (!repo.existsById(id)) {
            throw new ResourceNotFoundException("Product " + id + " not found");
        }
        repo.deleteById(id);
    }

    @Transactional(readOnly = true)
    public ImageData getImage(int id) {
        Product product = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product " + id + " not found"));
        byte[] data = product.getImageData();
        if (data == null || data.length == 0 || product.getImageType() == null) {
            throw new ResourceNotFoundException("Product " + id + " has no image");
        }
        return new ImageData(product.getImageType(), data);
    }

    private void applyFields(Product product, ProductRequest request) {
        product.setName(request.name().trim());
        product.setDescription(request.description());
        product.setBrand(request.brand().trim());
        product.setPrice(request.price());
        product.setCategory(request.category().trim());
        product.setReleaseDate(request.releaseDate());
        product.setProductAvailable(Boolean.TRUE.equals(request.productAvailable()));
        product.setStockQuantity(request.stockQuantity());
    }

    private void applyImage(Product product, MultipartFile image) {
        if (image == null || image.isEmpty()) {
            return;
        }
        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("Only image files can be uploaded");
        }
        try {
            product.setImageData(image.getBytes());
        } catch (IOException e) {
            throw new BadRequestException("Could not read the uploaded image");
        }
        product.setImageName(image.getOriginalFilename());
        product.setImageType(contentType);
    }
}
