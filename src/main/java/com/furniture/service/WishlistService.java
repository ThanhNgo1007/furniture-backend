package com.furniture.service;

import com.furniture.modal.Product;
import com.furniture.modal.User;
import com.furniture.modal.Wishlist;

public interface WishlistService {

    Wishlist createWishlist(User user);
    Wishlist getWishlistByUserId(User user);
    Wishlist addProductToWishlist(User user, Product product);
}
