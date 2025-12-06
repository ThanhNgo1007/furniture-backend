package com.furniture.service;

import java.util.List;

import com.furniture.modal.Product;
import com.furniture.modal.Review;
import com.furniture.modal.User;
import com.furniture.request.CreateReviewRequest;

public interface ReviewService {

    Review createReview(CreateReviewRequest request,
                        User user,
                        Product product) throws Exception;
    List<Review> getReviewsByProductId(Long productId);

    Review updateReview(Long reviewId,
                        String reviewText,
                        double rating,
                        Long userId) throws Exception;

    void deleteReview(Long reviewId, Long userId) throws Exception;

    Review getReviewById(Long reviewId) throws Exception;
    
    boolean hasUserPurchasedProduct(Long userId, Long productId);

}
