package com.furniture.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.furniture.domain.OrderStatus;
import com.furniture.domain.PaymentMethod;
import com.furniture.domain.PaymentOrderStatus;
import com.furniture.domain.PaymentStatus;
import com.furniture.modal.Address;
import com.furniture.modal.Cart;
import com.furniture.modal.CartItem;
import com.furniture.modal.Order;
import com.furniture.modal.OrderItem;
import com.furniture.modal.PaymentOrder;
import com.furniture.modal.Product;
import com.furniture.modal.User;
import com.furniture.repository.AddressRepository;
import com.furniture.repository.OrderItemRepository;
import com.furniture.repository.OrderRepository;
import com.furniture.repository.PaymentOrderRepository;
import com.furniture.repository.ProductRepository;
import com.furniture.service.OrderService;
import com.furniture.service.TransactionService;
import com.furniture.utils.VNPayUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final AddressRepository addressRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final TransactionService transactionService;
    private final PaymentOrderRepository paymentOrderRepository;

    @Override
    public Set<Order> createOrder(User user, Address shippingAddress, Cart cart) {

        Address address = addressRepository.save(shippingAddress);

        // 2. Kiểm tra an toàn: Chỉ thêm vào User nếu chưa có địa chỉ ID này
        boolean isAddressLinked = user.getAddresses().stream()
                .anyMatch(a -> a.getId().equals(address.getId()));

        if (!isAddressLinked) {
            user.getAddresses().add(address);
        }

        Map<Long, List<CartItem>> itemsBySeller = cart.getCartItemsInBag().stream()
                .collect(Collectors.groupingBy(item->item.getProduct().getSeller().getId()));
        Set<Order> orders = new HashSet<>();

        for(Map.Entry<Long, List<CartItem>> entry : itemsBySeller.entrySet()) {
            Long sellerId = entry.getKey();
            List<CartItem> items = entry.getValue();

            BigDecimal totalOrderPrice = items.stream()
                    .map(CartItem::getSellingPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            int totalItem = items.stream().mapToInt(CartItem::getQuantity).sum();

            Order createdOrder = new Order();
            createdOrder.setUser(user);
            createdOrder.setSellerId(sellerId);
            createdOrder.setTotalMsrpPrice(totalOrderPrice);
            createdOrder.setTotalSellingPrice(totalOrderPrice);
            createdOrder.setTotalItem(totalItem);
            createdOrder.setShippingAddress(address);
            createdOrder.setOrderStatus(OrderStatus.PENDING);
            createdOrder.getPaymentDetails().setStatus(PaymentStatus.PENDING);
            createdOrder.getPaymentDetails().setPaymentMethod(PaymentMethod.COD); // Default to COD


            createdOrder.setOrderId("ORD-" + VNPayUtil.getRandomNumber(6));


            Order savedOrder = orderRepository.save(createdOrder);
            orders.add(savedOrder);

            List<OrderItem> orderItems = new ArrayList<>();

            for(CartItem cartItem : items) {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(savedOrder);
                orderItem.setProduct(cartItem.getProduct());
                orderItem.setQuantity(cartItem.getQuantity());
                orderItem.setMsrpPrice(cartItem.getMsrpPrice());
                orderItem.setSellingPrice(cartItem.getSellingPrice());
                orderItem.setUserId(cartItem.getUserId());

                savedOrder.getOrderItems().add(orderItem);

                OrderItem savedOrderItem = orderItemRepository.save(orderItem);


                orderItems.add(savedOrderItem);
            }

        }

        return orders;
    }

    @Override
    public Order findOrderById(Long id) throws Exception {
        return orderRepository.findById(id).orElseThrow(()->
                new Exception("Order not found ..."));
    }

    @Override
    public List<Order> usersOrderHistory(Long userId) {
        return orderRepository.findByUserIdOrderByOrderDateDesc(userId);
    }

    @Override
    public List<Order> sellersOrder(Long sellerId) {
        return orderRepository.findBySellerIdOrderByOrderDateDesc(sellerId);
    }

    @Override
    public Order updateOrderStatus(Long orderId, OrderStatus orderStatus) throws Exception {
        Order order = findOrderById(orderId);
        order.setOrderStatus(orderStatus);

        // LOGIC MỚI: Xử lý khi giao hàng thành công
        if (orderStatus.equals(OrderStatus.DELIVERED)) {

            // Lấy PaymentOrder từ repository thay vì từ order.getPaymentOrder()
            PaymentOrder paymentOrder = paymentOrderRepository.findByOrderId(order.getId())
                    .orElse(null);

            // 1. Xử lý COD: Cập nhật trạng thái thanh toán
            if (paymentOrder != null && paymentOrder.getPaymentMethod().equals(PaymentMethod.COD)) {

                // Update status trong Order
                order.getPaymentDetails().setStatus(PaymentStatus.COMPLETED);
                order.setPaymentStatus(PaymentStatus.COMPLETED);

                // Update status trong PaymentOrder (Bảng cha)
                paymentOrder.setStatus(PaymentOrderStatus.SUCCESS);
                paymentOrderRepository.save(paymentOrder);
            }

            // 2. Tạo Transaction cho Seller
            // Chỉ tạo transaction khi đơn hàng đã hoàn thành thanh toán
            if (order.getPaymentDetails().getStatus().equals(PaymentStatus.COMPLETED)) {
                transactionService.createTransaction(order);
            }
        }

        return orderRepository.save(order);
    }

    @Override
    public Order cancelOrder(Long orderId, User user) throws Exception {
        Order order = findOrderById(orderId);

        if(!user.getId().equals(order.getUser().getId())) {
            throw new Exception("You don't have permission to cancel this order");
        }

        // --- 3. THÊM LOGIC HOÀN TRẢ SỐ LƯỢNG KHO ---
        // Chỉ cộng lại nếu đơn hàng ĐÃ ĐẶT (PLACED) hoặc ĐÃ XÁC NHẬN (CONFIRMED)
        // Lý do: Nếu đơn hàng đang PENDING (ví dụ chờ thanh toán VNPAY), kho chưa bị trừ nên không cần cộng.
        if (order.getOrderStatus().equals(OrderStatus.PLACED) ||
                order.getOrderStatus().equals(OrderStatus.CONFIRMED) ||
                order.getOrderStatus().equals(OrderStatus.SHIPPED)) {

            for (OrderItem item : order.getOrderItems()) {
                Product product = item.getProduct();

                // Cộng lại số lượng đã mua vào kho
                int newQuantity = product.getQuantity() + item.getQuantity();
                product.setQuantity(newQuantity);

                productRepository.save(product);
            }
        }
        // ---------------------------------------------

        order.setOrderStatus(OrderStatus.CANCELLED);
        // Có thể cập nhật thêm trạng thái thanh toán nếu cần, ví dụ:
        // if (order.getPaymentDetails().getStatus().equals(PaymentStatus.COMPLETED)) { ...logic hoàn tiền... }

        return orderRepository.save(order);
    }

    @Override
    public OrderItem getOrderItemById(Long id) throws Exception {
        return orderItemRepository.findById(id).orElseThrow(()->
                new Exception("Order item not found with id "));
    }

}
