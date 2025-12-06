package com.furniture.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.furniture.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import com.furniture.modal.Order;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
