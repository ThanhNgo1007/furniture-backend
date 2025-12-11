package com.furniture.repository;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.furniture.modal.Product;

import jakarta.persistence.LockModeType;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    //List<Product> findBySellerId(Long id);
    //Page<Product> findBySellerId(Long id, Pageable pageable);

    @Query("""
    SELECT p FROM Product p
    LEFT JOIN p.seller s
    WHERE
        (:query IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')))
        OR
        (:query IS NULL OR LOWER(p.category.name) LIKE LOWER(CONCAT('%', :query, '%')))
        OR
        (:query IS NULL OR LOWER(s.bussinessDetails.bussinessName) LIKE LOWER(CONCAT('%', :query, '%')))
""")
    List<Product> searchProduct(@Param("query") String query, Pageable pageable);

    long countBySellerIdAndQuantityLessThan(Long sellerId, int quantity);
    
    long countBySellerId(Long sellerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    //Optional<Product> findByIdWithLock(@Param("id") Long id);
    
    List<Product> findBySellerIdAndIsActive(Long sellerId, boolean isActive);
    
    /**
     * Best Seller: Get products with most sales (from delivered orders)
     * Returns products sorted by total quantity sold
     */
    @Query("""
        SELECT p, COALESCE(SUM(oi.quantity), 0) as totalSold
        FROM Product p
        LEFT JOIN OrderItem oi ON oi.product.id = p.id
        LEFT JOIN oi.order o ON o.orderStatus = com.furniture.domain.OrderStatus.DELIVERED
        WHERE p.isActive = true
        GROUP BY p
        ORDER BY totalSold DESC
    """)
    List<Object[]> findBestSellerProducts(Pageable pageable);
    
    /**
     * Similar Products: Get products with same category (level 3)
     * Excludes the current product
     */
    @Query("""
        SELECT p FROM Product p
        WHERE p.category.id = :categoryId
        AND p.id != :excludeProductId
        AND p.isActive = true
        ORDER BY p.numRatings DESC
    """)
    List<Product> findSimilarProducts(
        @Param("categoryId") Long categoryId,
        @Param("excludeProductId") Long excludeProductId,
        Pageable pageable
    );
}

