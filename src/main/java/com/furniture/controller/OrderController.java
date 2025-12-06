package com.furniture.controller;

import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.furniture.domain.OrderStatus;
import com.furniture.domain.PaymentMethod;
import com.furniture.domain.PaymentStatus;
import com.furniture.modal.Address;
import com.furniture.modal.Cart;
import com.furniture.modal.Order;
import com.furniture.modal.OrderItem;
import com.furniture.modal.PaymentOrder;
import com.furniture.modal.Product;
import com.furniture.modal.User;
import com.furniture.repository.OrderRepository;
import com.furniture.repository.ProductRepository;
import com.furniture.response.PaymentLinkResponse;
import com.furniture.service.CartService;
import com.furniture.service.OrderService;
import com.furniture.service.PaymentService;
import com.furniture.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;
    private final CartService cartService;
    private final PaymentService paymentService;

    // --- 1. THÊM 2 REPOSITORY NÀY ---
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    // --------------------------------

    @PostMapping()
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public ResponseEntity<PaymentLinkResponse> createOrderHandler(
            @RequestBody Address shippingAddress,
            @RequestParam PaymentMethod paymentMethod,
            @RequestHeader("Authorization") String jwt,
            HttpServletRequest request
    ) throws Exception {

        User user = userService.findUserByJwtToken(jwt);
        Cart cart = cartService.findUserCart(user);

        // Tạo đơn hàng (Lúc này trạng thái là PENDING)
        Set<Order> orders = orderService.createOrder(user, shippingAddress, cart);

        PaymentOrder paymentOrder = paymentService.createOrder(user, orders, paymentMethod);

        PaymentLinkResponse res = new PaymentLinkResponse();

        if (paymentMethod.equals(PaymentMethod.VNPAY)) {
            String paymentUrl = paymentService.createVnpayPaymentLink(
                    user,
                    paymentOrder.getAmount(),
                    paymentOrder.getId(),
                    request
            );

            res.setPayment_link_url(paymentUrl);
            PaymentOrder updatedPaymentOrder = paymentService.getPaymentOrderById(paymentOrder.getId());
            res.setPayment_link_id(updatedPaymentOrder.getPaymentLinkId());

        } else if (paymentMethod.equals(PaymentMethod.COD)) {
            // --- 2. CẬP NHẬT LOGIC CHO COD TẠI ĐÂY ---

            for (Order order : orders) {
                // A. Cập nhật trạng thái đơn hàng thành PLACED (Đã đặt)
                order.setOrderStatus(OrderStatus.PLACED);
                order.setPaymentStatus(PaymentStatus.PENDING); // COD thì chưa thanh toán tiền
                orderRepository.save(order);

                // B. Trừ số lượng sản phẩm trong kho
                for (OrderItem item : order.getOrderItems()) {
                    Product product = item.getProduct();
                    int newQuantity = product.getQuantity() - item.getQuantity();

                    if (newQuantity < 0) newQuantity = 0;

                    product.setQuantity(newQuantity);
                    productRepository.save(product);
                }
            }

            // C. Xóa giỏ hàng
            cartService.clearCart(user);

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
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public  ResponseEntity<Order> getOrderById(
            @PathVariable Long orderId,
            @RequestHeader("Authorization") String jwt
    ) throws Exception {
        // Xác thực user (đảm bảo JWT hợp lệ)
        User user = userService.findUserByJwtToken(jwt);
        Order order = orderService.findOrderById(orderId);
        
        // CHECK IDOR: Kiểm tra xem order có thuộc về user này không
        if (!order.getUser().getId().equals(user.getId())) {
            throw new Exception("You don't have permission to view this order");
        }
        
        return new ResponseEntity<>(order, HttpStatus.ACCEPTED);
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

        // ✅ REFACTORED: SellerReport (canceledOrders, totalRefunds) 
        // will be updated by batch job every 5 minutes
        // No immediate update needed

        return ResponseEntity.ok(order);
    }


}
