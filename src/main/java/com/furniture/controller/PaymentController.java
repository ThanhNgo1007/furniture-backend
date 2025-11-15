package com.furniture.controller;

import com.furniture.modal.*;
import com.furniture.response.ApiResponse;
import com.furniture.service.*;
import com.furniture.utils.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService paymentService;
    private final UserService userService;
    private final SellerService sellerService;
    private final SellerReportService sellerReportService;
    private final TransactionService transactionService;

    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse> paymentSuccessHandler(
            @PathVariable String paymentId,
            @RequestParam String paymentLinkId,
            @RequestParam(required = false, defaultValue = "00") String vnp_ResponseCode,
            @RequestParam(required = false, defaultValue = "00") String vnp_TransactionStatus,
            @RequestHeader("Authorization") String jwt
    ) throws Exception {

        // Xác thực user (đảm bảo JWT hợp lệ)
        userService.findUserByJwtToken(jwt);

        PaymentOrder paymentOrder = paymentService.getPaymentOrderByPaymentId(paymentId);

        Boolean success = paymentService.proceedPaymentOrder(
                paymentOrder,
                paymentId,
                paymentLinkId,
                vnp_ResponseCode,
                vnp_TransactionStatus
        );

        if(success){
            for(Order order : paymentOrder.getOrders()){
                transactionService.createTransaction(order);
                Seller seller = sellerService.getSellerById(order.getSellerId());
                SellerReport report = sellerReportService.getSellerReport(seller);
                report.setTotalOrders(report.getTotalOrders() + 1);
                report.setTotalEarnings(report.getTotalEarnings() + order.getTotalSellingPrice());
                report.setTotalSales(report.getTotalSales() + order.getOrderItems().size());
                sellerReportService.updateSellerReport(report);
            }
        }
        ApiResponse response = new ApiResponse();
        response.setMessage("Payment successful");

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // API Return (User quay về sau khi thanh toán)
    @GetMapping("/vnpay/return")
    public ResponseEntity<?> vnpayReturn(HttpServletRequest request) throws Exception {
        Map<String, String> params = VNPayUtil.getAllRequestParams(request);
        // Trả về 1 redirect (302) về ReactJS
        return paymentService.vnpayReturn(params);
    }
}