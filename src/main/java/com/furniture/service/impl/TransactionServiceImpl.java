package com.furniture.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.furniture.modal.Order;
import com.furniture.modal.Seller;
import com.furniture.modal.Transaction;
import com.furniture.repository.SellerRepository;
import com.furniture.repository.TransactionRepository;
import com.furniture.service.TransactionService;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final SellerRepository sellerRepository;
    private final com.furniture.repository.OrderRepository orderRepository;

    @Override
    public Transaction createTransaction(@NonNull Order order) {

        Long sellerId = order.getSellerId();
        if (sellerId == null) {
             throw new IllegalArgumentException("Order must have a seller ID");
        }
        Seller seller = sellerRepository.findById(sellerId).get();

        Transaction transaction = new Transaction();
        transaction.setSeller(seller);
        transaction.setCustomer(order.getUser());
        transaction.setOrder(order);
        transaction.setPaid(false);

        return transactionRepository.save(transaction);
    }

    @Override
    public List<Transaction> getTransactionsBySellerId(Seller seller) {
        // Trigger sync before fetching
        syncTransactionsForSeller(seller.getId());
        return transactionRepository.findBySellerId(seller.getId());
    }

    @Override
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public List<Transaction> processPayout(@NonNull Seller seller) {
        List<Transaction> unpaidTransactions = transactionRepository.findBySellerIdAndPaidFalse(seller.getId());

        for (Transaction tr : unpaidTransactions) {
            tr.setPaid(true); // Đánh dấu đã rút tiền
            tr.setPayoutDate(java.time.LocalDateTime.now()); // Lưu ngày rút tiền
        }

        if (unpaidTransactions != null) {
            return transactionRepository.saveAll(unpaidTransactions);
        }
        return List.of();
    }

    // Sync missing transactions for DELIVERED orders
    public void syncTransactionsForSeller(Long sellerId) {
        List<Order> deliveredOrders = orderRepository.findBySellerIdAndOrderStatus(sellerId, com.furniture.domain.OrderStatus.DELIVERED);
        
        for (Order order : deliveredOrders) {
            if (!transactionRepository.existsByOrder(order)) {
                createTransaction(order);
            }
        }
    }
}
