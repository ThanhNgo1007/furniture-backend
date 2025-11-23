package com.furniture.service;

import com.furniture.modal.Order;
import com.furniture.modal.PaymentOrder;
import com.furniture.modal.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

public interface PaymentService {
    PaymentOrder createOrder(User user, Set<Order> orders);

    PaymentOrder getPaymentOrderById(Long orderId) throws Exception;

    PaymentOrder getPaymentOrderByPaymentId(String paymentId) throws Exception;

    // --- THAY ĐỔI HÀM NÀY ---
    // Thêm 2 tham số trạng thái từ VNPAY
    Boolean proceedPaymentOrder(PaymentOrder paymentOrder,
                                String paymentId,
                                String paymentLinkId,
                                String vnpResponseCode,
                                String vnpTransactionStatus) throws Exception;
    // --- KẾT THÚC THAY ĐỔI ---

    // --- HÀM MỚI BẠN YÊU CẦU ---
    String createVnpayPaymentLink(User user, BigDecimal amount, Long paymentOrderId, HttpServletRequest request) throws Exception;

    // Đổi Map<String, String> thành ResponseEntity<?> (để redirect)
    ResponseEntity<?> vnpayReturn(Map<String, String> params) throws Exception;
}