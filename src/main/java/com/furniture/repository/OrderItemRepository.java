package com.furniture.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.furniture.modal.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    /**
     * Calculate total sales (sum of quantities) for delivered orders
     */
    @Query(
        "SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi " +
        "WHERE oi.order.sellerId = :sellerId " +
        "AND oi.order.orderStatus = com.furniture.domain.OrderStatus.DELIVERED"
    )
    Long sumQuantityBySellerIdForDeliveredOrders(@Param("sellerId") Long sellerId);
}
