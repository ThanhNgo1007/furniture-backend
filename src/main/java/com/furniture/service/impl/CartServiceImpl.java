// File: src/main/java/com/furniture/service/impl/CartServiceImpl.java
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
    public CartItem addCartItem(User user, Product product, int quantity) throws Exception {
        // 1. Gọi findUserCart để đảm bảo luôn có Cart (tránh null)
        Cart cart = findUserCart(user);

        // Kiểm tra tồn kho
        if (product.getQuantity() < quantity) {
            throw new Exception("Sản phẩm " + product.getTitle() + " không đủ số lượng.");
        }

        CartItem isPresent = cartItemRepository.findByCartAndProduct(cart, product);

        if (isPresent == null) {
            CartItem cartItem = new CartItem();
            cartItem.setProduct(product);
            cartItem.setCart(cart);
            cartItem.setQuantity(quantity);
            cartItem.setUserId(user.getId());

            // Tính giá bằng BigDecimal
            BigDecimal quantityBD = BigDecimal.valueOf(quantity);

            // Lấy giá từ Product (đã là BigDecimal)
            BigDecimal sellingPrice = product.getSellingPrice();
            BigDecimal msrpPrice = product.getMsrpPrice();

            cartItem.setSellingPrice(sellingPrice.multiply(quantityBD));
            cartItem.setMsrpPrice(msrpPrice.multiply(quantityBD));

            cart.getCartItemsInBag().add(cartItem);

            return cartItemRepository.save(cartItem);
        }
        // Nếu đã có thì trả về item cũ (hoặc bạn có thể code thêm logic cộng dồn số lượng)
        return isPresent;
    }

    @Override
    public Cart findUserCart(User user) {
        Cart cart = cartRepository.findByUserId(user.getId());

        // 2. Tự động tạo Cart nếu chưa có
        if (cart == null) {
            cart = new Cart();
            cart.setUser(user);
            cart.setTotalItem(0);
            cart.setTotalSellingPrice(BigDecimal.ZERO);
            cart.setTotalMsrpPrice(BigDecimal.ZERO);
            cart.setDiscount(0);
            cart = cartRepository.save(cart);
        }

        // 3. Tính toán lại tổng tiền Cart (Dùng BigDecimal)
        BigDecimal totalPrice = BigDecimal.ZERO;
        BigDecimal totalSellingPrice = BigDecimal.ZERO;
        int totalItem = 0;

        for (CartItem cartItem : cart.getCartItemsInBag()) {
            totalItem += cartItem.getQuantity();

            // Null safe add
            BigDecimal itemMsrp = cartItem.getMsrpPrice() != null ? cartItem.getMsrpPrice() : BigDecimal.ZERO;
            BigDecimal itemSelling = cartItem.getSellingPrice() != null ? cartItem.getSellingPrice() : BigDecimal.ZERO;

            totalPrice = totalPrice.add(itemMsrp);
            totalSellingPrice = totalSellingPrice.add(itemSelling);
        }

        cart.setTotalMsrpPrice(totalPrice);
        cart.setTotalSellingPrice(totalSellingPrice);
        cart.setTotalItem(totalItem);
        cart.setDiscount(calculateDiscountPercentage(totalPrice, totalSellingPrice));

        return cartRepository.save(cart);
    }

    private int calculateDiscountPercentage(BigDecimal msrpPrice, BigDecimal sellingPrice) {
        if (msrpPrice == null || msrpPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        BigDecimal discount = msrpPrice.subtract(sellingPrice);

        // (discount / msrp) * 100
        return discount.divide(msrpPrice, 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .intValue();
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
}
