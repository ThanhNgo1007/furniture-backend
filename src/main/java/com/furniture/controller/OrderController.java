package com.furniture.controller;

import com.furniture.domain.PaymentMethod;
import com.furniture.modal.*;
import com.furniture.response.PaymentLinkResponse;
import com.furniture.service.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;
    private final CartService cartService;
    private final SellerService sellerService;
    private final SellerReportService sellerReportService;
    private final PaymentService paymentService;

//    @PostMapping()
//    public ResponseEntity<PaymentLinkResponse> createOrderHandler(
//            @RequestBody Address shippingAddress,
//            @RequestParam PaymentMethod paymentMethod,
//            @RequestHeader("Authorization") String jwt
//    ) throws Exception {
//
//        User user = userService.findUserByJwtToken(jwt);
//        Cart cart = cartService.findUserCart(user);
//        Set<Order> orders = orderService.createOrder(user, shippingAddress, cart);
//
//       // PaymentOrder paymentOrder = paymentService.createOrder(user, orders);
//
//        PaymentLinkResponse res = new PaymentLinkResponse();
//
////        if (paymentMethod.equals(PaymentMethod.VNPAY)) {
////            PaymentLink payment = paymentService.createVnPayPaymentLink(user,
////                    paymentOrder.getAmount(),
////                    paymentOrder.getId());
////            String paymentUrl = payment.get("short_url");
////            String paymentUrlId = payment.get("id");
////
////
////            res.setPayment_link_url(paymentUrl);
////
////            paymentOrder.setPaymentLinkId(paymentUrlId);
////            paymentOrderRepository.save(paymentOrder);
////        }
//
//        return new ResponseEntity<>(res, HttpStatus.OK);
//    }
@PostMapping()
public ResponseEntity<PaymentLinkResponse> createOrderHandler(
        @RequestBody Address shippingAddress,
        @RequestParam PaymentMethod paymentMethod,
        @RequestHeader("Authorization") String jwt,
        HttpServletRequest request
) throws Exception {

    User user = userService.findUserByJwtToken(jwt);
    Cart cart = cartService.findUserCart(user);
    Set<Order> orders = orderService.createOrder(user, shippingAddress, cart);

    PaymentOrder paymentOrder = paymentService.createOrder(user, orders);

    PaymentLinkResponse res = new PaymentLinkResponse();

    if (paymentMethod.equals(PaymentMethod.VNPAY)) {

        // --- SỬA LOGIC GỌI ---
        // Gọi hàm mới với các tham số yêu cầu
        String paymentUrl = paymentService.createVnpayPaymentLink(
                user,
                paymentOrder.getAmount(),
                paymentOrder.getId(),
                request
        );

        res.setPayment_link_url(paymentUrl);

        // Lấy payment_link_id (vnp_TxnRef) đã được lưu bên trong service
        paymentOrder.setPaymentLinkId(paymentOrder.getPaymentLinkId());
        res.setPayment_link_id(paymentOrder.getPaymentLinkId());

        // Lưu lại PaymentOrder (bước này đã được thực hiện bên trong service,
        // nhưng gọi lại cũng không sao, hoặc bạn có thể bỏ qua dòng save)
        // paymentOrderRepository.save(paymentOrder);
        // --- KẾT THÚC SỬA ---

    } else if (paymentMethod.equals(PaymentMethod.COD)) {
        // Xử lý COD
        res.setPayment_link_url(null);
        res.setPayment_link_id(null);
    }

    return new ResponseEntity<>(res, HttpStatus.OK);
}
    @GetMapping("/user")
    public ResponseEntity<List<Order>> userOrderHistoryHandler(
            @RequestHeader("Authorization") String jwt
    ) throws Exception {
        User user = userService.findUserByJwtToken(jwt);
        List<Order> orders = orderService.usersOrderHistory(user.getId());
        return new ResponseEntity<>(orders, HttpStatus.ACCEPTED);
    }

    @GetMapping("/{orderId}")
    public  ResponseEntity<Order> getOrderById(
            @PathVariable Long orderId,
            @RequestHeader("Authorization") String jwt
    ) throws Exception {
        // Xác thực user (đảm bảo JWT hợp lệ)
        userService.findUserByJwtToken(jwt);
        Order orders = orderService.findOrderById(orderId);
        return new ResponseEntity<>(orders, HttpStatus.ACCEPTED);
    }

    @GetMapping("/item/{orderItemId}")
    public ResponseEntity<OrderItem> getOrderItemById(
            @PathVariable Long orderItemId,
            @RequestHeader("Authorization") String jwt
    ) throws Exception {
        System.out.println("------ controller");
        // Xác thực user (đảm bảo JWT hợp lệ)
        userService.findUserByJwtToken(jwt);
        OrderItem orderItem = orderService.getOrderItemById(orderItemId);
        return new ResponseEntity<>(orderItem, HttpStatus.ACCEPTED);
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<Order> cancelOrder(
            @PathVariable Long orderId,
            @RequestHeader("Authorization") String jwt
    ) throws Exception {
        User user = userService.findUserByJwtToken(jwt);
        Order order = orderService.cancelOrder(orderId, user);

        Seller seller = sellerService.getSellerById(order.getSellerId());
        SellerReport report = sellerReportService.getSellerReport(seller);

        report.setCanceledOrders(report.getCanceledOrders() + 1);
        report.setTotalRefunds(report.getTotalRefunds() + order.getTotalSellingPrice());
        sellerReportService.updateSellerReport(report);

        return ResponseEntity.ok(order);
    }


}
