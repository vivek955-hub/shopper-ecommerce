package com.example.ecom_project.repo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.example.ecom_project.dto.ProductResponse;
import com.example.ecom_project.model.Product;

@DataJpaTest
class ProductRepoTest {

    @Autowired
    private ProductRepo repo;

    @BeforeEach
    void setUp() {
        repo.deleteAll();
        repo.save(product("ProBook 14", "Light laptop", "Acme", "Laptop", 10, true));
        repo.save(product("UltraBook", "Thin laptop", "Acme", "Laptop", 5, true));
        repo.save(product("StudioBuds", "Wireless headphones", "Sonique", "Headphone", 20, true));
        repo.save(product("Nova Phone", "Smartphone", "Novatech", "Mobile", 7, true));
        repo.save(product("Retro Radio", "Vintage style radio", "Echoly", "Electronics", 3, true));
    }

    private static Product product(String name, String description, String brand, String category,
                                   int stock, boolean available) {
        Product p = new Product();
        p.setName(name);
        p.setDescription(description);
        p.setBrand(brand);
        p.setPrice(new BigDecimal("100.00"));
        p.setCategory(category);
        p.setReleaseDate(LocalDate.of(2024, 1, 1));
        p.setProductAvailable(available);
        p.setStockQuantity(stock);
        return p;
    }

    private static PageRequest firstPage(int size) {
        return PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "id"));
    }

    @Test
    void searchWithEmptyFiltersReturnsEverything() {
        Page<ProductResponse> result = repo.search("", "", firstPage(10));

        assertEquals(5, result.getTotalElements());
    }

    @Test
    void searchMatchesKeywordCaseInsensitivelyAcrossFields() {
        // matches on name
        assertEquals(1, repo.search("probook", "", firstPage(10)).getTotalElements());
        // matches on description and on brand
        assertEquals(2, repo.search("LAPTOP", "", firstPage(10)).getTotalElements());
        assertEquals(2, repo.search("acme", "", firstPage(10)).getTotalElements());
        // matches on description only
        assertEquals(1, repo.search("vintage", "", firstPage(10)).getTotalElements());
    }

    @Test
    void searchWithUnknownKeywordReturnsNothing() {
        assertTrue(repo.search("does-not-exist", "", firstPage(10)).isEmpty());
    }

    @Test
    void categoryFilterIsCaseInsensitive() {
        Page<ProductResponse> result = repo.search("", "laptop", firstPage(10));

        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().stream().allMatch(p -> p.category().equals("Laptop")));
    }

    @Test
    void keywordAndCategoryCanBeCombined() {
        Page<ProductResponse> result = repo.search("ultra", "Laptop", firstPage(10));

        assertEquals(1, result.getTotalElements());
        assertEquals("UltraBook", result.getContent().get(0).name());
    }

    @Test
    void resultsArePaged() {
        Page<ProductResponse> first = repo.search("", "", PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "id")));
        Page<ProductResponse> last = repo.search("", "", PageRequest.of(2, 2, Sort.by(Sort.Direction.DESC, "id")));

        assertEquals(5, first.getTotalElements());
        assertEquals(3, first.getTotalPages());
        assertEquals(2, first.getContent().size());
        assertEquals(1, last.getContent().size());
    }

    @Test
    void decrementStockSucceedsOnlyWhenEnoughStock() {
        Product saved = repo.save(product("Limited", "Only a few", "Acme", "Toys", 3, true));

        assertEquals(1, repo.decrementStock(saved.getId(), 2));
        assertEquals(1, repo.findById(saved.getId()).orElseThrow().getStockQuantity());

        // only 1 left, asking for 2 must not change anything
        assertEquals(0, repo.decrementStock(saved.getId(), 2));
        assertEquals(1, repo.findById(saved.getId()).orElseThrow().getStockQuantity());
    }

    @Test
    void decrementStockIgnoresUnavailableProducts() {
        Product saved = repo.save(product("Hidden", "Not for sale", "Acme", "Toys", 10, false));

        assertEquals(0, repo.decrementStock(saved.getId(), 1));
        assertEquals(10, repo.findById(saved.getId()).orElseThrow().getStockQuantity());
    }

    @Test
    void productIsMarkedUnavailableWhenStockReachesZero() {
        Product saved = repo.save(product("Last one", "Final unit", "Acme", "Toys", 1, true));

        assertEquals(1, repo.decrementStock(saved.getId(), 1));
        assertEquals(1, repo.markUnavailableIfOutOfStock(saved.getId()));

        Product reloaded = repo.findById(saved.getId()).orElseThrow();
        assertEquals(0, reloaded.getStockQuantity());
        assertFalse(reloaded.isProductAvailable());
    }

    @Test
    void markUnavailableDoesNothingWhileStockRemains() {
        Product saved = repo.save(product("Plenty", "Lots left", "Acme", "Toys", 5, true));

        assertEquals(0, repo.markUnavailableIfOutOfStock(saved.getId()));
        assertTrue(repo.findById(saved.getId()).orElseThrow().isProductAvailable());
    }
}
