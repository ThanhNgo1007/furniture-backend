package com.furniture.service;

import java.math.BigDecimal;
import java.util.List;

import com.furniture.modal.Cart;
import com.furniture.modal.Coupon;
import com.furniture.modal.User;

public interface CouponService {

    Cart applyCoupon(String code, BigDecimal orderValue, User user) throws Exception;
    Cart removeCoupon(String code, User user) throws Exception;
    Coupon findCouponById(Long id) throws Exception;
    Coupon createCoupon(Coupon coupon);
    Coupon updateCoupon(Long id, Coupon coupon) throws Exception;
    Coupon toggleCouponActive(Long id) throws Exception;
    List<Coupon> findAllCoupons();
    void deleteCoupon(Long id) throws Exception;
}

