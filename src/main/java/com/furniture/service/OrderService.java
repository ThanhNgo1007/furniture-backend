package com.furniture.service;

import java.util.List;
import java.util.Set;

import com.furniture.domain.OrderStatus;
import com.furniture.modal.Address;
import com.furniture.modal.Cart;
import com.furniture.modal.Order;
import com.furniture.modal.OrderItem;
import com.furniture.modal.User;

public interface OrderService {

    Set<Order> createOrder(User user, Address shippingAddress, Cart cart);
    Order findOrderById(Long id) throws Exception;
    List<Order> usersOrderHistory(Long userId);
    List<Order> sellersOrder(Long sellerId);
    Order updateOrderStatus(Long orderId, OrderStatus orderStatus) throws Exception;
    Order cancelOrder(Long orderId, User user) throws Exception;
    OrderItem getOrderItemById(Long id) throws Exception;

}
