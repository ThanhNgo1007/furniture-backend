// File: ecommerce-furniture/src/main/java/com/furniture/controller/PaymentController.java

package com.furniture.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.furniture.domain.PaymentOrderStatus;
import com.furniture.modal.PaymentOrder;
import com.furniture.modal.User;
import com.furniture.response.ApiResponse;
import com.furniture.service.CartService;
import com.furniture.service.PaymentService;
import com.furniture.service.UserService;
import com.furniture.utils.VNPayUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService paymentService;
    private final UserService userService;

    private final CartService cartService;

    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse> paymentSuccessHandler(
            @PathVariable String paymentId,
            @RequestParam String paymentLinkId,
            // SỬA: Xóa defaultValue="00" hoặc đổi thành mã khác (VD: "error") để tránh hiểu nhầm là thành công
            @RequestParam(required = false) String vnp_ResponseCode,
            @RequestParam(required = false) String vnp_TransactionStatus,
            @RequestHeader("Authorization") String jwt
    ) throws Exception {
        if (vnp_ResponseCode == null) vnp_ResponseCode = "error";
        if (vnp_TransactionStatus == null) vnp_TransactionStatus = "error";

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

            // ✅ REFACTORED: SellerReport will be updated by batch job every 5 minutes
            // No need to update immediately on every order
            
            // Clear cart after successful payment
            cartService.clearCart(user);
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