package com.furniture.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.furniture.domain.OrderStatus;
import com.furniture.modal.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // Sửa tên hàm để tự động sắp xếp giảm dần theo ngày (Mới nhất lên đầu)
    List<Order> findByUserIdOrderByOrderDateDesc(Long userId);

    List<Order> findBySellerIdOrderByOrderDateDesc(Long sellerId);
    
    // Query for finding orders by user and status
    List<Order> findByUserIdAndOrderStatus(Long userId, OrderStatus status);

    // --- Statistics Queries ---
    long countBySellerId(Long sellerId);

    long countBySellerIdAndOrderStatus(Long sellerId, com.furniture.domain.OrderStatus status);
    
    List<Order> findBySellerIdAndOrderStatus(Long sellerId, com.furniture.domain.OrderStatus status);

    @Query("SELECT SUM(o.totalSellingPrice) FROM Order o WHERE o.sellerId = :sellerId AND o.orderStatus = :status")
    BigDecimal sumTotalSellingPriceBySellerIdAndStatus(@org.springframework.data.repository.query.Param("sellerId") Long sellerId, @Param("status") OrderStatus status);

    // Fetch orders for Java-side aggregation (safer for Date handling)
    List<Order> findBySellerIdAndOrderDateBetween(Long sellerId, java.time.LocalDateTime startDate, LocalDateTime endDate);

    List<Order> findBySellerIdAndDeliveryDateBetween(Long sellerId, java.time.LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT COUNT(DISTINCT o.user.id) FROM Order o WHERE o.sellerId = :sellerId")
    long countDistinctUserBySellerId(@Param("sellerId") Long sellerId);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(o.totalMsrpPrice - o.totalSellingPrice) FROM Order o WHERE o.sellerId = :sellerId")
    BigDecimal sumTotalDiscountBySellerId(@Param("sellerId") Long sellerId);

    // --- Batch Report Aggregation Queries ---
    
    /**
     * Calculate total earnings for delivered orders (COALESCE handles null)
     */
    @Query(
        "SELECT COALESCE(SUM(o.totalSellingPrice), 0) FROM Order o " +
        "WHERE o.sellerId = :sellerId AND o.orderStatus = com.furniture.domain.OrderStatus.DELIVERED"
    )
    BigDecimal sumEarningsBySellerId(@Param("sellerId") Long sellerId);
    
    /**
     * Calculate total refunds for cancelled orders
     */
    @Query(
        "SELECT COALESCE(SUM(o.totalSellingPrice), 0) FROM Order o " +
        "WHERE o.sellerId = :sellerId AND o.orderStatus = com.furniture.domain.OrderStatus.CANCELLED"
    )
    java.math.BigDecimal sumRefundsBySellerId(@Param("sellerId") Long sellerId);
    
    /**
     * Count total orders (all statuses except PENDING)
     */
    @Query(
        "SELECT COUNT(o) FROM Order o " +
        "WHERE o.sellerId = :sellerId AND o.orderStatus != com.furniture.domain.OrderStatus.PENDING"
    )
    Long countTotalOrdersBySellerId(@Param("sellerId") Long sellerId);

    // ==================== ADMIN DASHBOARD QUERIES ====================
    
    /**
     * Total revenue (all DELIVERED orders)
     */
    @Query("SELECT COALESCE(SUM(o.totalSellingPrice), 0) FROM Order o WHERE o.orderStatus = :status")
    BigDecimal sumTotalRevenueByStatus(@Param("status") OrderStatus status);

    /**
     * Count orders by status (global)
     */
    long countByOrderStatus(OrderStatus status);

    /**
     * Count orders by date range
     */
    long countByOrderDateBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Sum revenue by date range and status
     */
    @Query("SELECT COALESCE(SUM(o.totalSellingPrice), 0) FROM Order o " +
           "WHERE o.orderDate BETWEEN :start AND :end AND o.orderStatus = :status")
    BigDecimal sumRevenueByDateRangeAndStatus(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end,
        @Param("status") OrderStatus status
    );

    /**
     * Average order value by status
     */
    @Query("SELECT AVG(o.totalSellingPrice) FROM Order o WHERE o.orderStatus = :status")
    BigDecimal avgOrderValueByStatus(@Param("status") OrderStatus status);

    /**
     * Find orders by delivery date range and status
     */
    List<Order> findByDeliveryDateBetweenAndOrderStatus(
        LocalDateTime start, LocalDateTime end, OrderStatus status
    );

    /**
     * Find orders by order date range (global)
     */
    List<Order> findByOrderDateBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Top sellers by revenue (returns sellerId, sellerName, totalRevenue, orderCount)
     */
    @Query("SELECT o.sellerId, s.sellerName, SUM(o.totalSellingPrice), COUNT(o) " +
           "FROM Order o JOIN Seller s ON o.sellerId = s.id " +
           "WHERE o.orderStatus = com.furniture.domain.OrderStatus.DELIVERED " +
           "GROUP BY o.sellerId, s.sellerName " +
           "ORDER BY SUM(o.totalSellingPrice) DESC " +
           "LIMIT :limit")
    List<Object[]> findTopSellersByRevenue(@Param("limit") int limit);

    /**
     * Count orders by payment method
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.paymentDetails.paymentMethod = :method")
    long countByPaymentMethod(@Param("method") com.furniture.domain.PaymentMethod method);

    /**
     * Sum revenue by payment method
     */
    @Query("SELECT COALESCE(SUM(o.totalSellingPrice), 0) FROM Order o " +
           "WHERE o.paymentDetails.paymentMethod = :method AND o.orderStatus = com.furniture.domain.OrderStatus.DELIVERED")
    BigDecimal sumRevenueByPaymentMethod(@Param("method") com.furniture.domain.PaymentMethod method);

    // ==================== CURSOR PAGINATION QUERIES ====================
    
    /**
     * Cursor-based pagination for seller orders
     * Uses order ID as cursor (descending order - newest first)
     */
    @Query("SELECT o FROM Order o WHERE o.sellerId = :sellerId " +
           "AND (:cursor IS NULL OR o.id < :cursor) " +
           "AND (:status IS NULL OR o.orderStatus = :status) " +
           "ORDER BY o.id DESC")
    List<Order> findBySellerIdWithCursor(
        @Param("sellerId") Long sellerId,
        @Param("cursor") Long cursor,
        @Param("status") OrderStatus status,
        org.springframework.data.domain.Pageable pageable);
    
    /**
     * Count total orders for seller (with optional status filter)
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.sellerId = :sellerId " +
           "AND (:status IS NULL OR o.orderStatus = :status)")
    long countBySellerIdAndOptionalStatus(
        @Param("sellerId") Long sellerId,
        @Param("status") OrderStatus status);

    /**
     * Cursor-based pagination for user orders
     */
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId " +
           "AND (:cursor IS NULL OR o.id < :cursor) " +
           "ORDER BY o.id DESC")
    List<Order> findByUserIdWithCursor(
        @Param("userId") Long userId,
        @Param("cursor") Long cursor,
        org.springframework.data.domain.Pageable pageable);
    
    /**
     * Count total orders for user
     */
    long countByUserId(Long userId);
}
