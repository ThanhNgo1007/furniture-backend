package com.furniture.service.impl;


import com.furniture.modal.Cart;
import com.furniture.modal.CartItem;
import com.furniture.modal.Product;
import com.furniture.modal.User;
import com.furniture.repository.CartItemRepository;
import com.furniture.repository.CartRepository;
import com.furniture.service.CartService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    @Override
    public CartItem addCartItem(User user, Product product, int quantity) {
        // Gọi hàm trên, đảm bảo cart KHÔNG BAO GIỜ null
        Cart cart = findUserCart(user);

        CartItem isPresent = cartItemRepository.findByCartAndProduct(cart, product);

        if (isPresent == null) {
            CartItem cartItem = new CartItem();
            cartItem.setProduct(product);
            cartItem.setCart(cart);
            cartItem.setQuantity(quantity);
            cartItem.setUserId(user.getId());

            // Tính giá (BigDecimal)
            BigDecimal quantityBD = BigDecimal.valueOf(quantity);
            cartItem.setSellingPrice(product.getSellingPrice().multiply(quantityBD));
            cartItem.setMsrpPrice(product.getMsrpPrice().multiply(quantityBD));

            cart.getCartItemsInBag().add(cartItem); // Giờ dòng này an toàn

            return cartItemRepository.save(cartItem);
        }

        // Nếu đã có item -> Update số lượng (Logic tùy chọn, có thể cộng dồn)
        return isPresent;
    }

    @Override
    @Transactional
    public void clearCart(User user) {
        Cart cart = cartRepository.findByUserId(user.getId());

        if (cart != null && !cart.getCartItemsInBag().isEmpty()) {
            // Xóa tất cả CartItem
            cartItemRepository.deleteAll(cart.getCartItemsInBag());

            // Clear collection
            cart.getCartItemsInBag().clear();

            // Reset giá trị
            cart.setTotalMsrpPrice(BigDecimal.ZERO);
            cart.setTotalSellingPrice(BigDecimal.ZERO);
            cart.setTotalItem(0);
            cart.setDiscount(0);
            cart.setCouponCode(null);

            // Lưu lại
            cartRepository.save(cart);
        }
    }

    @Override
    public Cart findUserCart(User user) {

        Cart cart = cartRepository.findByUserId(user.getId());

        if (cart == null) {
            cart = new Cart();
            cart.setUser(user);
            cart.setTotalItem(0);
            // Khởi tạo giá trị mặc định (BigDecimal.ZERO nếu bạn đã đổi, hoặc 0 nếu chưa)
            cart.setTotalSellingPrice(BigDecimal.ZERO);
            cart.setTotalMsrpPrice(BigDecimal.ZERO);
            cart = cartRepository.save(cart);
        }

        BigDecimal totalPrice = BigDecimal.ZERO;
        BigDecimal totalDiscountedPrice = BigDecimal.ZERO;
        int totalItem = 0;

        for (CartItem cartItem : cart.getCartItemsInBag()) {
            totalItem += cartItem.getQuantity();
            totalPrice = totalPrice.add(cartItem.getMsrpPrice());
            totalDiscountedPrice = totalDiscountedPrice.add(cartItem.getSellingPrice());
        }

        cart.setTotalMsrpPrice(totalPrice);
        cart.setTotalSellingPrice(totalDiscountedPrice);
        cart.setTotalItem(totalItem);
        cart.setDiscount(calculateDiscountPercentage(totalPrice, totalDiscountedPrice));
        cart.setTotalItem(totalItem);
        return cart;
    }

    private int calculateDiscountPercentage(BigDecimal msrpPrice, BigDecimal sellingPrice) {
        if (msrpPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        // (msrp - selling)
        BigDecimal discount = msrpPrice.subtract(sellingPrice);

        // (discount / msrp) * 100 -> Cần set scale và RoundingMode để tránh lỗi chia số lẻ vô hạn
        return discount.divide(msrpPrice, 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .intValue();
    }
}
