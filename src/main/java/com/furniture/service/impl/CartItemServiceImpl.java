// File: src/main/java/com/furniture/service/impl/CartItemServiceImpl.java
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

            // 1. Kiểm tra tồn kho
            int newQuantity = cartItem.getQuantity();
            int stock = item.getProduct().getQuantity();

            if (newQuantity > stock) {
                throw new Exception("Kho chỉ còn " + stock + " sản phẩm.");
            }

            item.setQuantity(newQuantity);

            // 2. Tính giá mới bằng BigDecimal
            BigDecimal quantityBD = BigDecimal.valueOf(newQuantity);

            BigDecimal productMsrp = item.getProduct().getMsrpPrice();
            BigDecimal productSelling = item.getProduct().getSellingPrice();

            item.setMsrpPrice(productMsrp.multiply(quantityBD));
            item.setSellingPrice(productSelling.multiply(quantityBD));

            return cartItemRepository.save(item);
        }
        throw new Exception("You can't update this cartItem");
    }

    // ... Các hàm remove, findById giữ nguyên
    @Override
    public void removeCartItem(Long userId, Long cartItemId) throws Exception {
        CartItem item = findCartItemById(cartItemId);
        User cartItemUser = item.getCart().getUser();
        if (cartItemUser.getId().equals(userId)) {
            cartItemRepository.delete(item);
        } else {
            throw new Exception("You can't delete this item");
        }
    }

    @Override
    public CartItem findCartItemById(Long id) throws Exception {
        return cartItemRepository.findById(id).orElseThrow(() ->
                new Exception("Cart item not found with id " + id));
    }
}