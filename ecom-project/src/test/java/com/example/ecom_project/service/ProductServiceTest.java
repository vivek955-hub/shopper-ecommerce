package com.example.ecom_project.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;

import com.example.ecom_project.dto.ImageData;
import com.example.ecom_project.dto.PageResponse;
import com.example.ecom_project.dto.ProductRequest;
import com.example.ecom_project.dto.ProductResponse;
import com.example.ecom_project.exception.BadRequestException;
import com.example.ecom_project.exception.ResourceNotFoundException;
import com.example.ecom_project.model.Product;
import com.example.ecom_project.repo.ProductRepo;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepo repo;

    @InjectMocks
    private ProductService service;

    private static ProductRequest request() {
        return new ProductRequest("  Phone ", "A phone", " Acme ", new BigDecimal("199.99"), "Mobile",
                null, true, 10);
    }

    @Test
    void getProductsClampsPageSizeAndTrimsFilters() {
        Page<ProductResponse> empty = new PageImpl<>(List.of());
        when(repo.search(any(), any(), any(Pageable.class))).thenReturn(empty);

        PageResponse<ProductResponse> response = service.getProducts("  phone ", null, -3, 1000);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(repo).search(org.mockito.ArgumentMatchers.eq("phone"), org.mockito.ArgumentMatchers.eq(""), captor.capture());
        assertEquals(0, captor.getValue().getPageNumber());
        assertEquals(ProductService.MAX_PAGE_SIZE, captor.getValue().getPageSize());
        assertEquals(0, response.content().size());
    }

    @Test
    void getByIdThrowsWhenMissing() {
        when(repo.findSummaryById(5)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getById(5));
    }

    @Test
    void createTrimsFieldsAndStoresImage() {
        when(repo.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        MockMultipartFile image = new MockMultipartFile("imageFile", "phone.png", "image/png", new byte[] {1, 2, 3});

        ProductResponse created = service.create(request(), image);

        assertEquals("Phone", created.name());
        assertEquals("Acme", created.brand());
        assertEquals("phone.png", created.imageName());
        assertEquals("image/png", created.imageType());
    }

    @Test
    void createRejectsNonImageUploads() {
        MockMultipartFile notAnImage = new MockMultipartFile("imageFile", "notes.txt", "text/plain", "hi".getBytes());

        assertThrows(BadRequestException.class, () -> service.create(request(), notAnImage));
        verify(repo, never()).save(any(Product.class));
    }

    @Test
    void updateKeepsExistingImageWhenNoNewImageIsUploaded() {
        Product existing = new Product();
        existing.setImageName("old.png");
        existing.setImageType("image/png");
        existing.setImageData(new byte[] {9, 9});
        when(repo.findById(1)).thenReturn(Optional.of(existing));
        when(repo.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponse updated = service.update(1, request(), null);

        assertEquals("Phone", updated.name());
        assertEquals("old.png", updated.imageName());
        assertArrayEquals(new byte[] {9, 9}, existing.getImageData());
    }

    @Test
    void updateThrowsWhenProductMissing() {
        when(repo.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.update(42, request(), null));
    }

    @Test
    void deleteThrowsWhenProductMissing() {
        when(repo.existsById(42)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.delete(42));
        verify(repo, never()).deleteById(anyInt());
    }

    @Test
    void getImageThrowsWhenProductHasNoImage() {
        Product withoutImage = new Product();
        when(repo.findById(1)).thenReturn(Optional.of(withoutImage));

        assertThrows(ResourceNotFoundException.class, () -> service.getImage(1));
    }

    @Test
    void getImageReturnsStoredBytesAndType() {
        Product withImage = new Product();
        withImage.setImageType("image/jpeg");
        withImage.setImageData(new byte[] {4, 5});
        when(repo.findById(1)).thenReturn(Optional.of(withImage));

        ImageData image = service.getImage(1);

        assertEquals("image/jpeg", image.contentType());
        assertArrayEquals(new byte[] {4, 5}, image.data());
    }
}
