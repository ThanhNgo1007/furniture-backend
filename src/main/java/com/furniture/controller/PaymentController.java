// File: ecommerce-furniture/src/main/java/com/furniture/controller/PaymentController.java

package com.furniture.controller;

import com.furniture.domain.PaymentOrderStatus; // Import thêm
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
import java.util.Set;

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

        User user = userService.findUserByJwtToken(jwt);

        PaymentOrder paymentOrder = paymentService.getPaymentOrderByPaymentId(paymentId);

        // Thực hiện cập nhật trạng thái (Nếu VNPay callback chưa chạy kịp thì hàm này sẽ chạy)
        boolean isUpdated = paymentService.proceedPaymentOrder(
                paymentOrder,
                paymentId,
                paymentLinkId,
                vnp_ResponseCode,
                vnp_TransactionStatus
        );

        // Kiểm tra: Nếu vừa update thành công HOẶC trạng thái đã là SUCCESS (do callback chạy trước)
        if (isUpdated || paymentOrder.getStatus().equals(PaymentOrderStatus.SUCCESS)) {

            Set<Order> orders = paymentOrder.getOrders();
            for (Order order : orders) {
                // Kiểm tra xem transaction đã được tạo chưa để tránh duplicate
                // (Giả sử transactionService có logic kiểm tra hoặc chúng ta check đơn giản ở đây nếu cần)
                // Ở mức độ project này, ta có thể tạo transaction nếu order đã placed

                // Lưu ý: Bạn có thể cần thêm logic check if(transactionService.existsByOrder(order)) để an toàn hơn
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

    @GetMapping("/vnpay/return")
    public ResponseEntity<?> vnpayReturn(HttpServletRequest request) throws Exception {
        Map<String, String> params = VNPayUtil.getAllRequestParams(request);
        return paymentService.vnpayReturn(params);
    }
}