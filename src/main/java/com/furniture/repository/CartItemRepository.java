package com.furniture.repository;

import com.furniture.modal.Cart;
import com.furniture.modal.CartItem;
import com.furniture.modal.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    CartItem findByCartAndProduct(Cart cart, Product product);
}
