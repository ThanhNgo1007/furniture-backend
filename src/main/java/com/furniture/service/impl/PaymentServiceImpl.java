package com.furniture.service.impl;

import com.furniture.config.VNPayConfig;
import com.furniture.domain.OrderStatus;
import com.furniture.domain.PaymentMethod;
import com.furniture.domain.PaymentOrderStatus;
import com.furniture.domain.PaymentStatus;
import com.furniture.modal.Order;
import com.furniture.modal.PaymentDetails;
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
public class  PaymentServiceImpl implements PaymentService {

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
    // File: .../service/impl/PaymentServiceImpl.java

    @Override
    @Transactional
    public Boolean proceedPaymentOrder(PaymentOrder paymentOrder,
                                       String paymentId, // Đây là vnp_TransactionNo (VD: 14234567)
                                       String paymentLinkId, // Đây là vnp_TxnRef (VD: 83452345)
                                       String vnp_ResponseCode,
                                       String vnp_TransactionStatus) throws Exception {

        if (paymentOrder.getStatus().equals(PaymentOrderStatus.PENDING)) {

            if ("00".equals(vnp_ResponseCode) && "00".equals(vnp_TransactionStatus)) {

                Set<Order> orders = paymentOrder.getOrders();

                for (Order order : orders) {
                    // 1. Cập nhật trạng thái đơn hàng
                    order.setPaymentStatus(PaymentStatus.COMPLETED);
                    order.setOrderStatus(OrderStatus.PLACED);

                    // 2. --- KHẮC PHỤC LỖI NULL Ở ĐÂY ---
                    // Bạn phải lấy PaymentDetails ra và set giá trị vào
                    if (order.getPaymentDetails() == null) {
                        order.setPaymentDetails(new PaymentDetails());
                    }

                    // Lưu vnp_TransactionNo vào paymentId
                    order.getPaymentDetails().setPaymentId(paymentId);

                    // Lưu vnp_TxnRef vào paymentLinkId
                    order.getPaymentDetails().setPaymentLinkId(paymentLinkId);

                    // Cập nhật trạng thái trong PaymentDetails
                    order.getPaymentDetails().setStatus(PaymentStatus.COMPLETED);

                    // 3. Lưu lại Order để Hibernate update xuống DB (bảng orders)
                    orderRepository.save(order);
                }

                // Cập nhật PaymentOrder cha
                paymentOrder.setStatus(PaymentOrderStatus.SUCCESS);
                paymentOrderRepository.save(paymentOrder);
                return true;
            }

            // Xử lý thất bại...
            paymentOrder.setStatus(PaymentOrderStatus.FAILED);
            paymentOrderRepository.save(paymentOrder);
            return false;
        }
        return false;
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
        String defaultCurrency = vnPayConfig.getDefaultCurrency();
        
        // VNPay yêu cầu amount phải nhân 100 (ví dụ: 1000 VND = 100000)
        long vnpAmount = amount * 100;

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

        // QUAN TRỌNG: Đây là đường dẫn trang kết quả ở Frontend (React)
        // Bạn có thể đổi "/payment/result" thành "/payment/success" cho dễ hiểu
        String frontendUrl = "http://localhost:5173/payment/success";

        if (signValue.equals(vnp_SecureHash)) {
            String vnp_ResponseCode = params.get("vnp_ResponseCode");
            String vnp_TxnRef = params.get("vnp_TxnRef"); // Đây là paymentLinkId

            // Redirect về Frontend kèm tham số
            // paymentId và paymentLinkId đều là vnp_TxnRef để khớp với Controller
            if ("00".equals(vnp_ResponseCode)) {
                return ResponseEntity.status(302).header("Location",
                        frontendUrl + "?paymentId=" + vnp_TxnRef + "&paymentLinkId=" + vnp_TxnRef + "&vnp_ResponseCode=" + vnp_ResponseCode).build();
            } else {
                return ResponseEntity.status(302).header("Location",
                        frontendUrl + "?error=true&vnp_ResponseCode=" + vnp_ResponseCode).build();
            }
        } else {
            return ResponseEntity.status(302).header("Location",
                    frontendUrl + "?error=checksum_failed").build();
        }
    }
}