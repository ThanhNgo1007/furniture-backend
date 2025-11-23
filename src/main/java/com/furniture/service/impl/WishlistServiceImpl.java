package com.furniture.service.impl;

import com.furniture.modal.Product;
import com.furniture.modal.User;
import com.furniture.modal.Wishlist;
import com.furniture.repository.WishlistRepository;
import com.furniture.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;

    @Override
    public Wishlist createWishlist(User user) {

        Wishlist wishlist = new Wishlist();
        wishlist.setUser(user);

        return wishlistRepository.save(wishlist);
    }

    @Override
    public Wishlist getWishlistByUserId(User user) {
        Wishlist wishlist =  wishlistRepository.findByUserId(user.getId());
        if (wishlist == null) {
            wishlist = createWishlist(user);
        }
        return wishlist;
    }

    @Override
    public Wishlist addProductToWishlist(User user, Product product) {
        Wishlist wishlist = getWishlistByUserId(user);
        boolean isPresent = false;

        // 1. Kiểm tra tồn tại bằng ID
        for (Product p : wishlist.getProducts()) {
            if (p.getId().equals(product.getId())) {
                isPresent = true;
                break;
            }
        }

        // 2. Logic TOGGLE (Dùng cho Product Details - Tim)
        if (isPresent) {
            wishlist.getProducts().removeIf(p -> p.getId().equals(product.getId()));
        } else {
            wishlist.getProducts().add(product);
        }

        return wishlistRepository.save(wishlist);
    }

    // --- HÀM MỚI: CHỈ XÓA (Dùng cho Wishlist - Nút X) ---
    @Override
    public Wishlist removeProductFromWishlist(User user, Product product) {
        Wishlist wishlist = getWishlistByUserId(user);

        // Chỉ thực hiện lệnh xóa, không bao giờ thêm
        wishlist.getProducts().removeIf(p -> p.getId().equals(product.getId()));

        return wishlistRepository.save(wishlist);
    }
}
