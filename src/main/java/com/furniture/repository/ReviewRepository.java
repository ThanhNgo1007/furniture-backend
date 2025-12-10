package com.furniture.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import com.furniture.modal.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProductId(Long productId);
    Page<Review> findByProductId(Long productId, org.springframework.data.domain.Pageable pageable);

}
