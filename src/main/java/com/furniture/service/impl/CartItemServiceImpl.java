package com.furniture.service.impl;

import com.furniture.modal.CartItem;
import com.furniture.modal.User;
import com.furniture.repository.CartItemRepository;
import com.furniture.service.CartItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CartItemServiceImpl implements CartItemService {

    private final CartItemRepository cartItemRepository;

    @Override
    public CartItem updateCartItem(Long userId, Long id, CartItem cartItem) throws Exception {
        CartItem item = findCartItemById(id);

        User cartItemUser = item.getCart().getUser();

        if (cartItemUser.getId().equals(userId)) {
            if (cartItem.getQuantity() > item.getProduct().getQuantity()) {
                throw new Exception("Kho chỉ còn " + item.getProduct().getQuantity() + " sản phẩm.");
            }
            item.setQuantity(cartItem.getQuantity());
            BigDecimal quantityBD = BigDecimal.valueOf(item.getQuantity());
            item.setMsrpPrice(item.getProduct().getMsrpPrice().multiply(quantityBD));
            item.setSellingPrice(item.getProduct().getSellingPrice().multiply(quantityBD));

            return cartItemRepository.save(item);

        }
        throw new Exception("You can't update this cartItem");
    }

    @Override
    public void removeCartItem(Long userId, Long cartItemId) throws Exception {

        CartItem item = findCartItemById(cartItemId);

        User cartItemUser = item.getCart().getUser();

        if (cartItemUser.getId().equals(userId)) {
            cartItemRepository.delete(item);
        }
        else throw new Exception("You can't delete this item");

    }

    @Override
    public CartItem findCartItemById(Long id) throws Exception {
        return cartItemRepository.findById(id).orElseThrow(() ->
                new Exception("Cart item not found with id " + id));
        
    }
}
