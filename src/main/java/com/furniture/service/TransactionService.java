package com.furniture.service;

import com.furniture.modal.Order;
import com.furniture.modal.Seller;
import com.furniture.modal.Transaction;

import java.util.List;

public interface TransactionService {

    Transaction createTransaction(Order order);
    List<Transaction> getTransactionsBySellerId(Seller seller);
    List<Transaction> getAllTransactions();
}
