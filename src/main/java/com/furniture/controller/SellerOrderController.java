package com.furniture.controller;

import java.util.List;

import com.furniture.response.CursorPageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.furniture.domain.OrderStatus;
import com.furniture.modal.Order;
import com.furniture.modal.Seller;
import com.furniture.service.OrderService;
import com.furniture.service.SellerService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/seller/orders")
public class SellerOrderController {

    private final OrderService orderService;
    private final SellerService sellerService;

    @GetMapping()
    public ResponseEntity<List<Order>> getAllOrdersHandler(
            @RequestHeader("Authorization") String jwt
    ) throws Exception {
        Seller seller  = sellerService.getSellerProfile(jwt);
        List<Order> orders = orderService.sellersOrder(seller.getId());

        return new ResponseEntity<>(orders, HttpStatus.ACCEPTED);
    }

    /**
     * Cursor-based paginated orders endpoint (recommended for large datasets)
     */
    @GetMapping("/paginated")
    public ResponseEntity<CursorPageResponse<Order>> getOrdersPaginated(
            @RequestHeader("Authorization") String jwt,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) com.furniture.domain.OrderStatus status
    ) throws Exception {
        Seller seller = sellerService.getSellerProfile(jwt);
        CursorPageResponse<Order> response =
            orderService.sellersOrderPaginated(seller.getId(), cursor, size, status);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{orderId}/status/{orderStatus}")
    public ResponseEntity<Order> updateOrderHandler(
            @RequestHeader("Authorization") String jwt,
            @PathVariable Long orderId,
            @PathVariable OrderStatus orderStatus
    ) throws Exception {

        Order order = orderService.updateOrderStatus(orderId, orderStatus);

        return new ResponseEntity<>(order, HttpStatus.ACCEPTED);
    }


}
