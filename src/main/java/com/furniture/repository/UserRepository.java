package com.furniture.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.furniture.domain.USER_ROLE;
import com.furniture.modal.User;

import jakarta.transaction.Transactional;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);

    // Count users by role
    long countByRole(USER_ROLE role);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO user_used_coupons (user_id, coupon_id) VALUES (:userId, :couponId)", nativeQuery = true)
    void insertUserUsedCoupon(@Param("userId") Long userId, @Param("couponId") Long couponId);

    @Query(value = "SELECT COUNT(*) FROM user_used_coupons WHERE user_id = :userId AND coupon_id = :couponId", nativeQuery = true)
    int countUserUsedCoupon(@Param("userId") Long userId, @Param("couponId") Long couponId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM user_used_coupons WHERE user_id = :userId AND coupon_id = :couponId", nativeQuery = true)
    void deleteUserUsedCoupon(@Param("userId") Long userId, @Param("couponId") Long couponId);
}
