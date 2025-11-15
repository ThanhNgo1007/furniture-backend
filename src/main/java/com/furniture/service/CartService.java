package com.furniture.service;

import com.furniture.modal.Cart;
import com.furniture.modal.CartItem;
import com.furniture.modal.Product;
import com.furniture.modal.User;

public interface CartService {

    public CartItem addCartItem(
            User user,
            Product product,
            int quantity
    );

    public Cart findUserCart(User user);
}
