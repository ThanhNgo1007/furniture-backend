package com.furniture.repository;

import com.furniture.modal.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // Sửa tên hàm để tự động sắp xếp giảm dần theo ngày (Mới nhất lên đầu)
    List<Order> findByUserIdOrderByOrderDateDesc(Long userId);

    List<Order> findBySellerIdOrderByOrderDateDesc(Long sellerId);
}
