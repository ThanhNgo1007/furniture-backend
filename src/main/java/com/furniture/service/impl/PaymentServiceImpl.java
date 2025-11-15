package com.furniture.service.impl;

import com.furniture.config.VNPayConfig;
import com.furniture.domain.OrderStatus;
import com.furniture.domain.PaymentMethod;
import com.furniture.domain.PaymentOrderStatus;
import com.furniture.domain.PaymentStatus;
import com.furniture.modal.Order;
import com.furniture.modal.PaymentOrder;
import com.furniture.modal.User;
import com.furniture.repository.OrderRepository;
import com.furniture.repository.PaymentOrderRepository;
import com.furniture.service.PaymentService;
import com.furniture.utils.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity; // <-- THÊM IMPORT
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentOrderRepository paymentOrderRepository;
    private final OrderRepository orderRepository;
    private final VNPayConfig vnPayConfig;

    @Override
    @Transactional
    public PaymentOrder createOrder(User user, Set<Order> orders) {
        Long amount = orders.stream()
                .mapToLong(Order::getTotalSellingPrice)
                .sum();

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

    // --- HÀM ĐÃ SỬA THEO YÊU CẦU CỦA BẠN (SỬA LỖI getPaymentDetails) ---
    @Override
    @Transactional
    public Boolean proceedPaymentOrder(PaymentOrder paymentOrder,
                                       String paymentId, // This is vnp_TransactionNo
                                       String paymentLinkId, // This is vnp_TxnRef
                                       String vnp_ResponseCode,
                                       String vnp_TransactionStatus) throws Exception {

        if (paymentOrder.getStatus().equals(PaymentOrderStatus.PENDING)) {

            // 1. Kiểm tra thanh toán thành công
            if ("00".equals(vnp_ResponseCode) && "00".equals(vnp_TransactionStatus)) {

                Set<Order> orders = paymentOrder.getOrders();

                // 2. Lặp qua các đơn hàng con
                for (Order order : orders) {
                    order.setPaymentStatus(PaymentStatus.COMPLETED);
                    order.setOrderStatus(OrderStatus.PLACED);

                    // 3. LƯU PAYMENT ID VÀO ORDER CON (ĐÂY LÀ CHỖ SỬA)
                    order.getPaymentDetails().setPaymentId(paymentId); //
                    order.getPaymentDetails().setStatus(PaymentStatus.COMPLETED); //

                    orderRepository.save(order);
                }

                // 4. Cập nhật PaymentOrder tổng
                paymentOrder.setStatus(PaymentOrderStatus.SUCCESS);
                paymentOrderRepository.save(paymentOrder);
                return true; // Thành công
            }

            // 5. Nếu thanh toán thất bại
            paymentOrder.setStatus(PaymentOrderStatus.FAILED);
            paymentOrderRepository.save(paymentOrder);
            return false; // Thất bại
        }
        return false; // Đã xử lý rồi
    }
    // --- KẾT THÚC HÀM SỬA ---

    // --- HÀM ĐÃ SỬA (THEO CHỮ KÝ HÀM BẠN YÊU CẦU) ---
    @Override
    public String createVnpayPaymentLink(User user, Long amount, Long paymentOrderId,
                                         HttpServletRequest request) throws Exception {

        // Lấy PaymentOrder và lưu vnp_TxnRef
        PaymentOrder paymentOrder = getPaymentOrderById(paymentOrderId);
        String vnp_TxnRef = VNPayUtil.getRandomNumber(10); // Mã giao dịch (duy nhất)
        paymentOrder.setPaymentLinkId(vnp_TxnRef); // Lưu mã này để kiểm tra khi VNPAY gọi về
        paymentOrderRepository.save(paymentOrder);

        // Convert amount từ USD sang VND nếu cần
        // VNPay chỉ hỗ trợ VND, nên nếu website dùng USD thì phải convert
        long amountInVnd = amount;
        String defaultCurrency = vnPayConfig.getDefaultCurrency();
        
        if ("USD".equalsIgnoreCase(defaultCurrency)) {
            // Convert USD sang VND
            double exchangeRate = vnPayConfig.getUsdToVndRate();
            amountInVnd = Math.round(amount * exchangeRate);
        }
        
        // VNPay yêu cầu amount phải nhân 100 (ví dụ: 1000 VND = 100000)
        long vnpAmount = amountInVnd * 100;

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

    @Override
    @Transactional
    public ResponseEntity<?> vnpayReturn(Map<String, String> params) throws Exception {
        String vnp_SecureHash = params.remove("vnp_SecureHash");
        if (params.containsKey("vnp_SecureHashType")) {
            params.remove("vnp_SecureHashType");
        }

        String hashData = VNPayUtil.getHashData(params);
        String signValue = VNPayUtil.hmacSHA512(vnPayConfig.getSecretKey(), hashData);

        // Đây là URL ReactJS (FE) của bạn
        String frontendUrl = "http://localhost:3000/payment/result";

        if (signValue.equals(vnp_SecureHash)) {
            String vnp_ResponseCode = params.get("vnp_ResponseCode");
            String vnp_TransactionStatus = params.get("vnp_TransactionStatus");
            String vnp_TxnRef = params.get("vnp_TxnRef");
            String vnp_TransactionNo = params.get("vnp_TransactionNo");

            // Xử lý thanh toán cho localhost
            PaymentOrder paymentOrder = paymentOrderRepository.findByPaymentLinkId(vnp_TxnRef);
            if (paymentOrder != null && paymentOrder.getStatus() == PaymentOrderStatus.PENDING) {
                // Chỉ xử lý nếu đơn hàng chưa được xử lý
                proceedPaymentOrder(paymentOrder, vnp_TransactionNo, vnp_TxnRef, vnp_ResponseCode, vnp_TransactionStatus);
            }

            // Redirect về frontend với thông tin kết quả
            if ("00".equals(vnp_ResponseCode) && "00".equals(vnp_TransactionStatus)) {
                return ResponseEntity.status(302).header("Location",
                        frontendUrl + "?success=true&vnp_TxnRef=" + vnp_TxnRef).build();
            } else {
                return ResponseEntity.status(302).header("Location",
                        frontendUrl + "?success=false&vnp_ResponseCode=" + vnp_ResponseCode).build();
            }
        } else {
            // Checksum không hợp lệ
            return ResponseEntity.status(302).header("Location",
                    frontendUrl + "?success=false&error=checksum_fail").build();
        }
    }
}