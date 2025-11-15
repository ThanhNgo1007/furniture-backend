package com.furniture.service.impl;


import com.furniture.modal.Cart;
import com.furniture.modal.CartItem;
import com.furniture.modal.Product;
import com.furniture.modal.User;
import com.furniture.repository.CartItemRepository;
import com.furniture.repository.CartRepository;
import com.furniture.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    @Override
    public CartItem addCartItem(User user, Product product, int quantity) {

        Cart cart = findUserCart(user);
        CartItem isPresent = cartItemRepository.findByCartAndProduct(cart, product);

        if (isPresent == null) {
            CartItem cartItem = new CartItem();
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItem.setUserId(user.getId());

            int totalPrice = quantity * product.getSellingPrice();
            cartItem.setSellingPrice(totalPrice);
            cartItem.setMsrpPrice(quantity * product.getMsrpPrice());

            cart.getCartItems().add(cartItem);
            cartItem.setCart(cart);

            return cartItemRepository.save(cartItem);
        }

        return isPresent;
    }

    @Override
    public Cart findUserCart(User user) {

        Cart cart = cartRepository.findByUserId(user.getId());

        int totalPrice = 0;
        int totalDiscountedPrice = 0;
        int totalItem = 0;

        for (CartItem cartItem : cart.getCartItems()) {
            totalItem += cartItem.getQuantity();
            totalPrice += cartItem.getMsrpPrice();
            totalDiscountedPrice += cartItem.getSellingPrice();
        }

        cart.setTotalMsrpPrice(totalPrice);
        cart.setTotalSellingPrice(totalDiscountedPrice);
        cart.setTotalItem(totalItem);
        cart.setDiscount(calculateDiscountPercentage(totalPrice, totalDiscountedPrice));
        cart.setTotalItem(totalItem);
        return cart;
    }

    private int calculateDiscountPercentage(int msrpPrice, int sellingPrice) {
        if (msrpPrice <= 0) {
            return 0;
        }
        double discount = msrpPrice - sellingPrice;
        double discountPercentage = (discount/msrpPrice) * 100.0;

        return (int)discountPercentage;
    }
}
