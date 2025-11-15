package com.furniture.repository;

import com.furniture.modal.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // <-- 1. THÊM IMPORT NÀY
import org.springframework.data.repository.query.Param; // <-- 2. THÊM IMPORT NÀY

public interface CartRepository extends JpaRepository<Cart, Long> {

//    // 3. THÊM @Query TƯỜNG MINH
//    @Query("SELECT c FROM Cart c WHERE c.user.id = :userId")
//    Cart findUserById(@Param("userId") Long userId);
        Cart findByUserId(Long userId);
}