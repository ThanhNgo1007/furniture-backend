package com.furniture.controller;

import com.furniture.modal.Product;
import com.furniture.modal.User;
import com.furniture.modal.Wishlist;
import com.furniture.repository.WishlistRepository;
import com.furniture.service.ProductService;
import com.furniture.service.UserService;
import com.furniture.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistRepository wishlistRepository;
    private final UserService userService;
    private final WishlistService wishlistService;
    private final ProductService productService;

    @GetMapping()
    public ResponseEntity<Wishlist> getWishlistByUserId(
            @RequestHeader("Authorization") String jwt) throws Exception {

        User user = userService.findUserByJwtToken(jwt);
        Wishlist wishlist = wishlistService.getWishlistByUserId(user);

        return ResponseEntity.ok(wishlist);
    }

    @PostMapping("/add-product/{productId}")
    public ResponseEntity<Wishlist> addProductToWishlist(
            @PathVariable Long productId,
            @RequestHeader("Authorization") String jwt) throws Exception {

        Product product = productService.findProductById(productId);
        User user = userService.findUserByJwtToken(jwt);
        Wishlist updatedWishlist = wishlistService.addProductToWishlist(user, product);

        return ResponseEntity.ok(updatedWishlist);
    }

    @PutMapping("/remove-product/{productId}")
    public ResponseEntity<Wishlist> removeProductFromWishlist(
            @PathVariable Long productId,
            @RequestHeader("Authorization") String jwt) throws Exception {

        Product product = productService.findProductById(productId);
        User user = userService.findUserByJwtToken(jwt);

        // Gọi hàm remove riêng biệt
        Wishlist updatedWishlist = wishlistService.removeProductFromWishlist(user, product);

        return ResponseEntity.ok(updatedWishlist);
    }
}
