package com.furniture.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.furniture.modal.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // Find all messages in a conversation (ascending order for chat display)
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId ORDER BY m.createdAt ASC")
    List<Message> findByConversationIdOrderByCreatedAtAsc(@Param("conversationId") Long conversationId);

    // Find messages with pagination (descending for loading history)
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId ORDER BY m.createdAt DESC")
    Page<Message> findByConversationIdOrderByCreatedAtDesc(
            @Param("conversationId") Long conversationId,
            Pageable pageable
    );

    // Count unread messages for a specific recipient
    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation.id = :conversationId " +
           "AND m.isRead = false AND m.senderId != :recipientId")
    Integer countUnreadMessages(
            @Param("conversationId") Long conversationId,
            @Param("recipientId") Long recipientId
    );

    // Mark all messages as read for a recipient and set readAt timestamp
    // Now checks sender_type since User and Seller can have the same ID in different tables
    @Modifying
    @Query("UPDATE Message m SET m.isRead = true, m.readAt = CURRENT_TIMESTAMP WHERE m.conversation.id = :conversationId " +
           "AND m.senderType != :recipientType AND m.isRead = false")
    void markAllAsRead(
            @Param("conversationId") Long conversationId,
            @Param("recipientType") String recipientType
    );
}
