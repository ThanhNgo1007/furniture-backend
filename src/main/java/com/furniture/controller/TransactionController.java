package com.furniture.controller;

import com.furniture.modal.Seller;
import com.furniture.modal.Transaction;
import com.furniture.response.ApiResponse;
import com.furniture.service.SellerService;
import com.furniture.service.TransactionService;
import com.furniture.service.impl.TransactionServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final SellerService sellerService;

    @GetMapping("/seller")
    public ResponseEntity<List<Transaction>> getTransactionBySeller(
        @RequestHeader("Authorization") String jwt
        ) throws Exception {
            Seller seller  = sellerService.getSellerProfile(jwt);
            List<Transaction> transactions = transactionService.getTransactionsBySellerId(seller);

            return ResponseEntity.ok(transactions);
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransaction(){
        List<Transaction> transactions = transactionService.getAllTransactions();
        return ResponseEntity.ok(transactions);
    }

    @PostMapping("/payout")
    public ResponseEntity<ApiResponse> payoutSeller(
            @RequestHeader("Authorization") String jwt
    ) throws Exception {
        Seller seller = sellerService.getSellerProfile(jwt);

        // Gọi service xử lý update paid = true
        // (Cần ép kiểu hoặc gọi trực tiếp service impl nếu interface chưa có, tốt nhất là update Interface)
        List<Transaction> transactions = ((TransactionServiceImpl) transactionService).processPayout(seller);

        ApiResponse res = new ApiResponse();
        res.setMessage("Payout successful. " + transactions.size() + " transactions processed.");

        return ResponseEntity.ok(res);
    }


}
