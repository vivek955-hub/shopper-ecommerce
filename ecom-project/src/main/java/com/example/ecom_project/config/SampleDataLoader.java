package com.example.ecom_project.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.example.ecom_project.model.Product;
import com.example.ecom_project.repo.ProductRepo;

/** Inserts a few demo products on startup when app.seed.enabled=true and the table is empty. */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class SampleDataLoader implements CommandLineRunner {

    private final ProductRepo repo;

    public SampleDataLoader(ProductRepo repo) {
        this.repo = repo;
    }

    @Override
    public void run(String... args) {
        if (repo.count() > 0) {
            return;
        }
        repo.saveAll(List.of(
                product("ProBook 14", "Lightweight 14-inch laptop with 16GB RAM", "Acme", "899.00", "Laptop", 25),
                product("UltraBook Air", "Thin and light laptop with all-day battery", "Acme", "1099.00", "Laptop", 12),
                product("StudioBuds", "Wireless noise-cancelling headphones", "Sonique", "149.99", "Headphone", 40),
                product("BassMax Over-Ear", "Over-ear headphones with deep bass", "Sonique", "89.50", "Headphone", 30),
                product("Nova X Phone", "6.5-inch AMOLED smartphone, 128GB", "Novatech", "499.00", "Mobile", 50),
                product("Nova Mini", "Compact smartphone with great camera", "Novatech", "329.00", "Mobile", 18),
                product("Smart Speaker", "Voice-controlled speaker for the home", "Echoly", "59.99", "Electronics", 60),
                product("Building Blocks Set", "500-piece creative building set", "BrickJoy", "24.99", "Toys", 100),
                product("Denim Jacket", "Classic fit denim jacket", "UrbanLine", "64.00", "Fashion", 35)));
    }

    private Product product(String name, String description, String brand, String price, String category, int stock) {
        Product p = new Product();
        p.setName(name);
        p.setDescription(description);
        p.setBrand(brand);
        p.setPrice(new BigDecimal(price));
        p.setCategory(category);
        p.setReleaseDate(LocalDate.now().minusDays(30));
        p.setProductAvailable(true);
        p.setStockQuantity(stock);
        return p;
    }
}
