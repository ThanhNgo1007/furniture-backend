package com.furniture.repository;

import com.furniture.modal.Order;
import com.furniture.modal.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findBySellerId(Long sellerId);
    List<Transaction> findBySellerIdAndPaidFalse(Long orderId);
    boolean existsByOrder(Order order);

}
