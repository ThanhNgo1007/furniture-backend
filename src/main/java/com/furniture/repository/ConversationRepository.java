package com.furniture.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.furniture.modal.Conversation;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    // Find conversation between user and seller (with optional order and product)
    @Query("SELECT c FROM Conversation c WHERE c.user.id = :userId AND c.seller.id = :sellerId " +
           "AND (:orderId IS NULL OR c.order.id = :orderId) " +
           "AND (:productId IS NULL OR c.product.id = :productId)")
    Optional<Conversation> findByUserIdAndSellerIdAndOrderIdAndProductId(
        Long userId, Long sellerId, Long orderId, Long productId);

    // Find existing conversation between user and seller (ignoring specific product/order context)
    @Query("SELECT c FROM Conversation c WHERE c.user.id = :userId AND c.seller.id = :sellerId")
    Optional<Conversation> findByUserIdAndSellerId(Long userId, Long sellerId);

    // Find all conversations for a user, ordered by last message time
    @Query("SELECT c FROM Conversation c WHERE c.user.id = :userId ORDER BY c.lastMessageAt DESC")
    List<Conversation> findByUserIdOrderByLastMessageAtDesc(Long userId);

    // Find all conversations for a seller, ordered by last message time
    @Query("SELECT c FROM Conversation c WHERE c.seller.id = :sellerId ORDER BY c.lastMessageAt DESC")
    List<Conversation> findBySellerIdOrderByLastMessageAtDesc(Long sellerId);

    // Count unread conversations for user
    @Query("SELECT COUNT(c) FROM Conversation c WHERE c.user.id = :userId AND c.unreadCountUser > 0")
    Long countUnreadByUserId(Long userId);

    // Count unread conversations for seller
    @Query("SELECT COUNT(c) FROM Conversation c WHERE c.seller.id = :sellerId AND c.unreadCountSeller > 0")
    Long countUnreadBySellerId(Long sellerId);
}
