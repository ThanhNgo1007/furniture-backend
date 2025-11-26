package com.furniture.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.furniture.modal.PaymentOrder;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long> {
    PaymentOrder findByPaymentLinkId(String paymentLinkId);
    
    @Query("SELECT po FROM PaymentOrder po JOIN po.orders o WHERE o.id = :orderId")
    Optional<PaymentOrder> findByOrderId(@Param("orderId") Long orderId);
}