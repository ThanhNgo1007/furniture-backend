package com.furniture.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.furniture.domain.OrderStatus;
import com.furniture.modal.Order;
import com.furniture.modal.Product;
import com.furniture.modal.Review;
import com.furniture.modal.User;
import com.furniture.repository.OrderRepository;
import com.furniture.repository.ReviewRepository;
import com.furniture.request.CreateReviewRequest;
import com.furniture.service.ReviewService;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;

    @Override
    public Review createReview(CreateReviewRequest request, User user, Product product) throws Exception {
        // Verify user has purchased the product
        if (!hasUserPurchasedProduct(user.getId(), product.getId())) {
            throw new Exception("You can only review products you have purchased and received");
        }
        
        Review review = new Review();
        review.setUser(user);
        review.setProduct(product);
        review.setReviewText(request.getReviewText());
        review.setRating(request.getReviewRating());
        review.setProductImages(request.getProductImages());
        review.setOrderId(request.getOrderId());

        product.getReviews().add(review);
        return reviewRepository.save(review);
    }

    @Override
    public List<Review> getReviewsByProductId(Long productId) {
        return reviewRepository.findByProductId(productId);
    }

    @Override
    public Review updateReview(Long reviewId, String reviewText, double rating, Long userId) throws Exception {
        Review review = getReviewById(reviewId);

        if (review.getUser().getId().equals(userId)) {
            review.setReviewText(reviewText);
            review.setRating(rating);
            return reviewRepository.save(review);
        }
        throw new Exception("You can not update this review");
    }

    @Override
    public void deleteReview(Long reviewId, Long userId) throws Exception {
        Review review = getReviewById(reviewId);
        if (review.getUser().getId().equals(userId)) {
            throw new Exception("You can not delete this review");
        }
        reviewRepository.delete(review);

    }

    @Override
    public Review getReviewById(@NonNull Long reviewId) throws Exception {
        return reviewRepository.findById(reviewId).orElseThrow(()->
                new Exception("review not found"));
    }
    
    @Override
    public boolean hasUserPurchasedProduct(Long userId, Long productId) {
        // Find all delivered orders for the user
        List<Order> deliveredOrders = orderRepository.findByUserIdAndOrderStatus(
                userId, OrderStatus.DELIVERED
        );
        
        // Check if any delivered order contains the product
        return deliveredOrders.stream()
                .flatMap(order -> order.getOrderItems().stream())
                .anyMatch(orderItem -> orderItem.getProduct().getId().equals(productId));
    }
}
