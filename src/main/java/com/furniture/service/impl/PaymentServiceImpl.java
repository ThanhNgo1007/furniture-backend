package com.furniture.service.impl;

import com.furniture.config.VNPayConfig;
import com.furniture.domain.OrderStatus;
import com.furniture.domain.PaymentMethod;
import com.furniture.domain.PaymentOrderStatus;
import com.furniture.domain.PaymentStatus;
import com.furniture.modal.*;
import com.furniture.repository.OrderRepository;
import com.furniture.repository.PaymentOrderRepository;
import com.furniture.repository.ProductRepository;
import com.furniture.service.PaymentService;
import com.furniture.utils.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity; // <-- THÊM IMPORT
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class  PaymentServiceImpl implements PaymentService {

    private final PaymentOrderRepository paymentOrderRepository;
    private final OrderRepository orderRepository;
    private final VNPayConfig vnPayConfig;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public PaymentOrder createOrder(User user, Set<Order> orders) {
        BigDecimal amount = orders.stream()
                .map(Order::getTotalSellingPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        PaymentOrder paymentOrder = new PaymentOrder();
        paymentOrder.setAmount(amount);
        paymentOrder.setUser(user);
        paymentOrder.setOrders(orders);
        // Gán phương thức thanh toán cho PaymentOrder
        paymentOrder.setPaymentMethod(PaymentMethod.VNPAY); // Hoặc lấy động
        paymentOrder.setStatus(PaymentOrderStatus.PENDING);

        return paymentOrderRepository.save(paymentOrder);
    }

    @Override
    public PaymentOrder getPaymentOrderById(Long orderId) throws Exception {
        return paymentOrderRepository.findById(orderId)
                .orElseThrow(() -> new Exception("Payment order not found with id: " + orderId));
    }

    @Override
    public PaymentOrder getPaymentOrderByPaymentId(String paymentId) throws Exception {
        // Sửa: Tìm bằng paymentLinkId (vnp_TxnRef)
        PaymentOrder paymentOrder = paymentOrderRepository.findByPaymentLinkId(paymentId);
        if (paymentOrder == null) {
            throw new Exception("Payment order not found with paymentId " + paymentId);
        }
        return paymentOrder;
    }

    @Override
    @Transactional
    public Boolean proceedPaymentOrder(PaymentOrder paymentOrder,
                                       String paymentId,
                                       String paymentLinkId,
                                       String vnp_ResponseCode,
                                       String vnp_TransactionStatus) throws Exception {

        // Kiểm tra nếu đơn hàng đã được xử lý rồi thì bỏ qua
        if (!paymentOrder.getStatus().equals(PaymentOrderStatus.PENDING)) {
            return false;
        }

        Set<Order> orders = paymentOrder.getOrders();

        // ===== XỬ LÝ THANH TOÁN THÀNH CÔNG =====
        if ("00".equals(vnp_ResponseCode) && "00".equals(vnp_TransactionStatus)) {
            for (Order order : orders) {
                order.setPaymentStatus(PaymentStatus.COMPLETED);
                order.setOrderStatus(OrderStatus.PLACED);

                if (order.getPaymentDetails() == null) {
                    order.setPaymentDetails(new PaymentDetails());
                }

                order.getPaymentDetails().setPaymentId(paymentId);
                order.getPaymentDetails().setPaymentLinkId(paymentLinkId);
                order.getPaymentDetails().setStatus(PaymentStatus.COMPLETED);

                orderRepository.save(order);

                for (OrderItem item : order.getOrderItems()) {
                    Product product = item.getProduct();
                    int newQuantity = product.getQuantity() - item.getQuantity();

                    // Đảm bảo không bị âm (phòng trường hợp race condition)
                    if (newQuantity < 0) newQuantity = 0;

                    product.setQuantity(newQuantity);
                    productRepository.save(product); // Lưu lại số lượng mới
                }
            }

            paymentOrder.setStatus(PaymentOrderStatus.SUCCESS);
            paymentOrderRepository.save(paymentOrder);
            return true;
        }

        // ===== XỬ LÝ HỦY THANH TOÁN / THẤT BẠI =====
        else {
            for (Order order : orders) {
                // Đánh dấu đơn hàng đã bị hủy
                order.setOrderStatus(OrderStatus.CANCELLED);
                order.setPaymentStatus(PaymentStatus.FAILED);

                if (order.getPaymentDetails() == null) {
                    order.setPaymentDetails(new PaymentDetails());
                }

                order.getPaymentDetails().setPaymentId(paymentId);
                order.getPaymentDetails().setPaymentLinkId(paymentLinkId);
                order.getPaymentDetails().setStatus(PaymentStatus.FAILED);

                orderRepository.save(order);
            }

            paymentOrder.setStatus(PaymentOrderStatus.FAILED);
            paymentOrderRepository.save(paymentOrder);
            return false;
        }
    }

    // --- HÀM ĐÃ SỬA (THEO CHỮ KÝ HÀM BẠN YÊU CẦU) ---
    @Override
    public String createVnpayPaymentLink(User user, BigDecimal amount, Long paymentOrderId,
                                         HttpServletRequest request) throws Exception {

        // Lấy PaymentOrder và lưu vnp_TxnRef
        PaymentOrder paymentOrder = getPaymentOrderById(paymentOrderId);
        String vnp_TxnRef = VNPayUtil.getRandomNumber(10); // Mã giao dịch (duy nhất)
        paymentOrder.setPaymentLinkId(vnp_TxnRef); // Lưu mã này để kiểm tra khi VNPAY gọi về
        paymentOrderRepository.save(paymentOrder);

        // Convert amount từ USD sang VND nếu cần
        // VNPay chỉ hỗ trợ VND, nên nếu website dùng USD thì phải convert
        String defaultCurrency = vnPayConfig.getDefaultCurrency();

        // VNPay yêu cầu amount phải nhân 100 (ví dụ: 1000 VND = 100000)
        long vnpAmount = paymentOrder.getAmount()
                .multiply(BigDecimal.valueOf(100))
                .longValue(); // Chuyển về long để gửi cho VNPay

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnPayConfig.getVersion());
        vnp_Params.put("vnp_Command", vnPayConfig.getCommand());
        vnp_Params.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        vnp_Params.put("vnp_Amount", String.valueOf(vnpAmount));
        vnp_Params.put("vnp_CurrCode", "VND"); // VNPay chỉ hỗ trợ VND
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang:" + paymentOrderId);
        vnp_Params.put("vnp_OrderType", vnPayConfig.getOrderType());
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        vnp_Params.put("vnp_IpAddr", VNPayUtil.getIpAddress(request)); // IP bắt buộc

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        // --- SỬA LỖI HASH ---
        // 1. Tạo hashData (KHÔNG encode)
        String hashData = VNPayUtil.getHashData(vnp_Params);

        // 2. Tạo checksum
        String vnp_SecureHash = VNPayUtil.hmacSHA512(vnPayConfig.getSecretKey(), hashData);
        vnp_Params.put("vnp_SecureHash", vnp_SecureHash); // Thêm hash vào params

        // 3. Tạo queryUrl (CÓ encode)
        String queryUrl = VNPayUtil.getQuery(vnp_Params);
        // --- KẾT THÚC SỬA ---

        return vnPayConfig.getVnpUrl() + "?" + queryUrl;
    }

    // File: .../service/impl/PaymentServiceImpl.java

    @Override
    @Transactional
    public ResponseEntity<?> vnpayReturn(Map<String, String> params) throws Exception {
        String vnp_SecureHash = params.remove("vnp_SecureHash");
        if (params.containsKey("vnp_SecureHashType")) {
            params.remove("vnp_SecureHashType");
        }

        String hashData = VNPayUtil.getHashData(params);
        String signValue = VNPayUtil.hmacSHA512(vnPayConfig.getSecretKey(), hashData);

        String frontendSuccessUrl = "http://localhost:5173/payment/success";
        String frontendFailedUrl = "http://localhost:5173/payment/failed";

        if (signValue.equals(vnp_SecureHash)) {
            String vnp_ResponseCode = params.get("vnp_ResponseCode");
            String vnp_TransactionStatus = params.get("vnp_TransactionStatus");
            String vnp_TxnRef = params.get("vnp_TxnRef");

            // ===== THANH TOÁN THÀNH CÔNG =====
            if ("00".equals(vnp_ResponseCode)) {
                PaymentOrder paymentOrder = paymentOrderRepository.findByPaymentLinkId(vnp_TxnRef);
                if (paymentOrder != null) {
                    proceedPaymentOrder(
                            paymentOrder,
                            params.get("vnp_TransactionNo"),
                            vnp_TxnRef,
                            vnp_ResponseCode,
                            vnp_TransactionStatus
                    );
                }

                return ResponseEntity.status(302)
                        .header("Location", frontendSuccessUrl +
                                "?paymentId=" + vnp_TxnRef +
                                "&paymentLinkId=" + vnp_TxnRef +
                                "&vnp_ResponseCode=" + vnp_ResponseCode)
                        .build();
            }

            // ===== THANH TOÁN THẤT BẠI / HỦY =====
            else {
                PaymentOrder paymentOrder = paymentOrderRepository.findByPaymentLinkId(vnp_TxnRef);
                if (paymentOrder != null) {
                    proceedPaymentOrder(
                            paymentOrder,
                            params.get("vnp_TransactionNo"),
                            vnp_TxnRef,
                            vnp_ResponseCode,
                            vnp_TransactionStatus
                    );
                }

                // ===== FIX: ENCODE MESSAGE TRƯỚC KHI GỬI =====
                String errorMessage = getVNPayErrorMessage(vnp_ResponseCode);
                String encodedMessage = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);

                return ResponseEntity.status(302)
                        .header("Location", frontendFailedUrl +
                                "?error=true&vnp_ResponseCode=" + vnp_ResponseCode +
                                "&message=" + encodedMessage) // <-- SỬ DỤNG encodedMessage
                        .build();
            }
        } else {
            return ResponseEntity.status(302)
                    .header("Location", frontendFailedUrl + "?error=checksum_failed")
                    .build();
        }
    }

    private String getVNPayErrorMessage(String responseCode) {
        return switch (responseCode) {
            case "24" -> "Giao dịch bị hủy bởi khách hàng";
            case "51" -> "Tài khoản không đủ số dư";
            case "65" -> "Tài khoản đã vượt quá hạn mức giao dịch";
            case "75" -> "Ngân hàng thanh toán đang bảo trì";
            case "79" -> "Giao dịch bị timeout";
            default -> "Giao dịch thất bại";
        };
    }
}