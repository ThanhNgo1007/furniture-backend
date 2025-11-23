package com.furniture.service.impl;

import com.furniture.modal.Cart;
import com.furniture.modal.Coupon;
import com.furniture.modal.User;
import com.furniture.repository.CartRepository;
import com.furniture.repository.CouponRepository;
import com.furniture.repository.UserRepository;
import com.furniture.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;


    @Override
    public Cart applyCoupon(String code, BigDecimal orderValue, User user) throws Exception {

        Coupon coupon = couponRepository.findByCode(code);

        Cart cart = cartRepository.findByUserId(user.getId());

        if (coupon == null) {
            throw new Exception("Invalid coupon code");
        }
        if(user.getUsedCoupons().contains(coupon)){
            throw new Exception("Coupon already used");
        }
        if (orderValue.compareTo(coupon.getMinimumOrderValue()) <= 0) {
            throw new Exception("Not enough minimum value: " + coupon.getMinimumOrderValue());
        }
        if(coupon.isActive() && LocalDate.now().isAfter(coupon.getValidityStartDate())
                && LocalDate.now().isBefore(coupon.getValidityEndDate())){
            user.getUsedCoupons().add(coupon);
            userRepository.save(user);

            BigDecimal currentTotal = cart.getTotalSellingPrice(); // Giả sử Cart đã đổi sang BigDecimal
            BigDecimal percentage = coupon.getDiscountPercentage();

            BigDecimal discountedPrice = currentTotal.multiply(percentage)
                    .divide(BigDecimal.valueOf(100));

            // 2. Trừ tiền: Total - DiscountedPrice
            cart.setTotalSellingPrice(currentTotal.subtract(discountedPrice));

            cart.setCouponCode(code);
            cartRepository.save(cart);
            return cart;
        }

        throw new Exception("Invalid coupon code");
    }

    @Override
    public Cart removeCoupon(String code, User user) throws Exception {
        Coupon coupon = couponRepository.findByCode(code);

        if (coupon == null) {
            throw new Exception("Coupon not found ...");
        }
        Cart cart = cartRepository.findByUserId(user.getId());

        BigDecimal currentTotal = cart.getTotalSellingPrice();
        BigDecimal percentage = coupon.getDiscountPercentage();

        // Hệ số còn lại: (100 - percentage) / 100
        BigDecimal remainingFactor = BigDecimal.valueOf(100).subtract(percentage)
                .divide(BigDecimal.valueOf(100));

        // Tính lại giá gốc: Price_Sau_Giam / Hệ số
        // Ví dụ: Giá 80k, Giảm 20% (còn 0.8). Giá gốc = 80 / 0.8 = 100k
        BigDecimal originalPrice = currentTotal.divide(remainingFactor);

        cart.setTotalSellingPrice(originalPrice);
        cart.setCouponCode(null);

        return cartRepository.save(cart);
    }

    @Override
    public Coupon findCouponById(Long id) throws Exception {

        return couponRepository.findById(id).orElseThrow(()->
                new Exception("Coupon not found"));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Coupon createCoupon(Coupon coupon) {
        return couponRepository.save(coupon);
    }

    @Override
    public List<Coupon> findAllCoupons() {
        return couponRepository.findAll();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCoupon(Long id) throws Exception {
        findCouponById(id);
        couponRepository.deleteById(id);
    }
}
