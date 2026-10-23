package com.example.ecom_project.repo;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.ecom_project.dto.ProductResponse;
import com.example.ecom_project.model.Product;

@Repository
public interface ProductRepo extends JpaRepository<Product, Integer> {

    /** Selects every column except the image bytes, so list queries stay light. */
    String PROJECTION = "new com.example.ecom_project.dto.ProductResponse("
            + "p.id, p.name, p.description, p.brand, p.price, p.category, p.releaseDate, "
            + "p.productAvailable, p.stockQuantity, p.imageName, p.imageType)";

    String SEARCH_FILTER = " from Product p where "
            + "(:category = '' or lower(p.category) = lower(:category)) and ("
            + "lower(p.name) like lower(concat('%', :keyword, '%')) or "
            + "lower(p.description) like lower(concat('%', :keyword, '%')) or "
            + "lower(p.brand) like lower(concat('%', :keyword, '%')) or "
            + "lower(p.category) like lower(concat('%', :keyword, '%')))";

    /** Pass "" (not null) for keyword / category to mean "no filter". */
    @Query(value = "select " + PROJECTION + SEARCH_FILTER, countQuery = "select count(p)" + SEARCH_FILTER)
    Page<ProductResponse> search(@Param("keyword") String keyword,
                                 @Param("category") String category,
                                 Pageable pageable);

    @Query("select " + PROJECTION + " from Product p where p.id = :id")
    Optional<ProductResponse> findSummaryById(@Param("id") int id);

    /**
     * Atomic stock decrement. The WHERE clause makes the database itself refuse to go
     * below zero, so two buyers racing for the last item cannot both succeed.
     * Returns the number of rows changed (0 = not enough stock or product unavailable).
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Product p set p.stockQuantity = p.stockQuantity - :qty "
            + "where p.id = :id and p.productAvailable = true and p.stockQuantity >= :qty")
    int decrementStock(@Param("id") int id, @Param("qty") int qty);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Product p set p.productAvailable = false where p.id = :id and p.stockQuantity = 0")
    int markUnavailableIfOutOfStock(@Param("id") int id);
}
