package com.furniture.service;

import com.furniture.modal.Product;
import com.furniture.modal.Review;
import com.furniture.modal.User;
import com.furniture.request.CreateReviewRequest;

import java.util.List;

public interface ReviewService {

    Review createReview(CreateReviewRequest request,
                        User user,
                        Product product);
    List<Review> getReviewsByProductId(Long productId);

    Review updateReview(Long reviewId,
                        String reviewText,
                        double rating,
                        Long userId) throws Exception;

    void deleteReview(Long reviewId, Long userId) throws Exception;

    Review getReviewById(Long reviewId) throws Exception;

}
