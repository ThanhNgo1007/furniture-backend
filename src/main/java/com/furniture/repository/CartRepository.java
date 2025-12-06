package com.furniture.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.furniture.modal.Cart;

public interface CartRepository extends JpaRepository<Cart, Long> {

//    // 3. THÊM @Query TƯỜNG MINH
//    @Query("SELECT c FROM Cart c WHERE c.user.id = :userId")
//    Cart findUserById(@Param("userId") Long userId);
        Cart findByUserId(Long userId);
}