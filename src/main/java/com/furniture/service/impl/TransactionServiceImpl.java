package com.furniture.service.impl;

import com.furniture.modal.Order;
import com.furniture.modal.Seller;
import com.furniture.modal.Transaction;
import com.furniture.repository.SellerRepository;
import com.furniture.repository.TransactionRepository;
import com.furniture.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final SellerRepository sellerRepository;

    @Override
    public Transaction createTransaction(Order order) {

        Seller seller = sellerRepository.findById(order.getSellerId()).get();

        Transaction transaction = new Transaction();
        transaction.setSeller(seller);
        transaction.setCustomer(order.getUser());
        transaction.setOrder(order);
        transaction.setPaid(false);

        return transactionRepository.save(transaction);
    }

    @Override
    public List<Transaction> getTransactionsBySellerId(Seller seller) {
        return transactionRepository.findBySellerId(seller.getId());
    }

    @Override
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public List<Transaction> processPayout(Seller seller) {
        List<Transaction> unpaidTransactions = transactionRepository.findBySellerIdAndPaidFalse(seller.getId());

        for (Transaction tr : unpaidTransactions) {
            tr.setPaid(true); // Đánh dấu đã rút tiền
        }

        return transactionRepository.saveAll(unpaidTransactions);
    }
}
