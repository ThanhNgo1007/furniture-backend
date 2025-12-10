package com.furniture.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.furniture.modal.Cart;
import com.furniture.modal.Coupon;
import com.furniture.modal.User;
import com.furniture.repository.CartRepository;
import com.furniture.repository.CouponRepository;
import com.furniture.repository.UserRepository;
import com.furniture.service.CouponService;

import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;


    @Override
    @org.springframework.transaction.annotation.Transactional
    public Cart applyCoupon(String code, BigDecimal orderValue, User user) throws Exception {
        // Ensure we are working with a managed user entity
        User managedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new Exception("User not found"));

        Coupon coupon = couponRepository.findByCode(code);
        Cart cart = cartRepository.findByUserId(managedUser.getId());

        if (coupon == null) {
            throw new Exception("Invalid coupon code");
        }
        
        // Use Native Query to check if coupon is used - Bypass Collection Loading
        int usedCount = userRepository.countUserUsedCoupon(managedUser.getId(), coupon.getId());
        if(usedCount > 0){
            throw new Exception("Coupon already used");
        }
        
        if (orderValue.compareTo(coupon.getMinimumOrderValue()) < 0) {
            throw new Exception("Not enough minimum value: " + coupon.getMinimumOrderValue());
        }
        
        LocalDate today = LocalDate.now();
        boolean isActive = coupon.isActive();
        boolean isStarted = today.isEqual(coupon.getValidityStartDate()) || today.isAfter(coupon.getValidityStartDate());
        boolean isNotExpired = today.isEqual(coupon.getValidityEndDate()) || today.isBefore(coupon.getValidityEndDate());

        if(isActive && isStarted && isNotExpired){
            // Insert into user_used_coupons via native query
            userRepository.insertUserUsedCoupon(managedUser.getId(), coupon.getId());

            // Calculate actual total from cart items
            BigDecimal actualTotal = BigDecimal.ZERO;
            for (var item : cart.getCartItemsInBag()) {
                BigDecimal itemTotal = item.getSellingPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                actualTotal = actualTotal.add(itemTotal);
            }
            
            BigDecimal percentage = coupon.getDiscountPercentage();

            // Calculate discount amount with proper rounding
            BigDecimal discountAmount = actualTotal.multiply(percentage)
                    .divide(BigDecimal.valueOf(100), 0, java.math.RoundingMode.HALF_UP);

            // Store discount PERCENTAGE for display (NOT amount)
            cart.setDiscount(percentage.intValue());
            
            // Set new total = actual total - discount amount
            cart.setTotalSellingPrice(actualTotal.subtract(discountAmount));
            cart.setCouponCode(code);
            
            return cartRepository.save(cart);
        }

        throw new Exception("Invalid coupon code");
    }

    @Override
    @Transactional
    public Cart removeCoupon(String code, User user) throws Exception {
        Coupon coupon = couponRepository.findByCode(code);

        if (coupon == null) {
            throw new Exception("Coupon not found ...");
        }
        
        Cart cart = cartRepository.findByUserId(user.getId());

        // Delete from user_used_coupons so user can reapply later
        userRepository.deleteUserUsedCoupon(user.getId(), coupon.getId());

        // Calculate actual total from cart items (correct value without any coupon discount)
        BigDecimal actualTotal = BigDecimal.ZERO;
        for (var item : cart.getCartItemsInBag()) {
            BigDecimal itemTotal = item.getSellingPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            actualTotal = actualTotal.add(itemTotal);
        }

        cart.setTotalSellingPrice(actualTotal);
        cart.setCouponCode(null);
        cart.setDiscount(0); // Reset discount

        return cartRepository.save(cart);
    }

    @Override
    public Coupon findCouponById(@NonNull Long id) throws Exception {

        return couponRepository.findById(id).orElseThrow(()->
                new Exception("Coupon not found"));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Coupon createCoupon(@NonNull Coupon coupon) {
        return couponRepository.save(coupon);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Coupon updateCoupon(@NonNull Long id, @NonNull Coupon coupon) throws Exception {
        Coupon existingCoupon = findCouponById(id);
        
        if (coupon.getCode() != null) {
            existingCoupon.setCode(coupon.getCode());
        }
        if (coupon.getDiscountPercentage() != null) {
            existingCoupon.setDiscountPercentage(coupon.getDiscountPercentage());
        }
        if (coupon.getValidityStartDate() != null) {
            existingCoupon.setValidityStartDate(coupon.getValidityStartDate());
        }
        if (coupon.getValidityEndDate() != null) {
            existingCoupon.setValidityEndDate(coupon.getValidityEndDate());
        }
        if (coupon.getMinimumOrderValue() != null) {
            existingCoupon.setMinimumOrderValue(coupon.getMinimumOrderValue());
        }
        
        return couponRepository.save(existingCoupon);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Coupon toggleCouponActive(@NonNull Long id) throws Exception {
        Coupon coupon = findCouponById(id);
        coupon.setActive(!coupon.isActive());
        return couponRepository.save(coupon);
    }

    @Override
    public List<Coupon> findAllCoupons() {
        return couponRepository.findAll();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCoupon(@NonNull Long id) throws Exception {
        findCouponById(id);
        couponRepository.deleteById(id);
    }
}
