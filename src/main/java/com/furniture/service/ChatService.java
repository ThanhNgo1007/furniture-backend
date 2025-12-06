package com.furniture.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.furniture.modal.Conversation;
import com.furniture.modal.Message;
import com.furniture.modal.Order;
import com.furniture.modal.Product;
import com.furniture.modal.Seller;
import com.furniture.modal.User;
import com.furniture.repository.ConversationRepository;
import com.furniture.repository.MessageRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final SellerService sellerService;
    private final org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    /**
     * Get or create conversation between user and seller (with optional order and product)
     */
    @Transactional
    public Conversation getOrCreateConversation(User user, Seller seller, Order order, Product product) {
        // Try to find ANY existing conversation between this user and seller
        Optional<Conversation> existing = conversationRepository.findByUserIdAndSellerId(user.getId(), seller.getId());

        Conversation conversation;
        boolean isNewProductContext = false;

        if (existing.isPresent()) {
            conversation = existing.get();
            // Check if product context is changing
            if (product != null && (conversation.getProduct() == null || !conversation.getProduct().getId().equals(product.getId()))) {
                conversation.setProduct(product);
                isNewProductContext = true;
            }
            if (order != null) {
                conversation.setOrder(order);
            }
            conversation = conversationRepository.save(conversation);
        } else {
            // Create new conversation
            conversation = Conversation.builder()
                    .user(user)
                    .seller(seller)
                    .order(order)
                    .product(product)
                    .unreadCountUser(0)
                    .unreadCountSeller(0)
                    .lastMessageAt(LocalDateTime.now())
                    .build();
            conversation = conversationRepository.save(conversation);
            if (product != null) {
                isNewProductContext = true;
            }
        }

        // Insert a system message about the product if context changed
        if (isNewProductContext && product != null) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                ProductInfo info = new ProductInfo(
                    product.getId(),
                    product.getTitle(),
                    (product.getImages() != null && !product.getImages().isEmpty()) ? product.getImages().get(0) : ""
                );
                String productContent = mapper.writeValueAsString(info);
                saveMessage(conversation, user.getId(), "USER", productContent, "PRODUCT");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return conversation;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    private static class ProductInfo {
        private Long id;
        private String title;
        private String image;
    }

    /**
     * Save a new message and update conversation
     */
    @Transactional
    public Message saveMessage(Conversation conversation, Long senderId, String senderType, 
                                String content, String messageType) {
        Message message = Message.builder()
                .conversation(conversation)
                .senderId(senderId)
                .senderType(senderType)
                .content(content)
                .messageType(messageType)
                .isRead(false)
                .build();

        Message savedMessage = messageRepository.save(message);

        // Update conversation's last message time and unread count
        conversation.setLastMessageAt(LocalDateTime.now());
        
        if ("USER".equals(senderType)) {
            conversation.setUnreadCountSeller(conversation.getUnreadCountSeller() + 1);
        } else {
            conversation.setUnreadCountUser(conversation.getUnreadCountUser() + 1);
        }
        
        conversationRepository.save(conversation);

        // Broadcast message to WebSocket topic
        try {
            String senderName = getSenderName(savedMessage.getSenderId(), savedMessage.getSenderType());
            com.furniture.response.ChatMessageResponse response = com.furniture.response.ChatMessageResponse.fromMessage(savedMessage, senderName);
            messagingTemplate.convertAndSend("/topic/conversation/" + conversation.getId(), response);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return savedMessage;
    }

    public Conversation findConversationById(Long conversationId) {
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found with id: " + conversationId));
    }

    public List<Conversation> getUserConversations(Long userId) {
        return conversationRepository.findByUserIdOrderByLastMessageAtDesc(userId);
    }

    public List<Conversation> getSellerConversations(Long sellerId) {
        return conversationRepository.findBySellerIdOrderByLastMessageAtDesc(sellerId);
    }

    public Page<Message> getChatHistory(Long conversationId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return messageRepository.findByConversationIdOrderByCreatedAtDesc(conversationId, pageable);
    }

    public List<Message> getAllMessages(Long conversationId) {
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    @Transactional
    public void markMessagesAsRead(Long conversationId, Long readerId, String readerType) {
        Conversation conversation = findConversationById(conversationId);
        
        // Reset unread count
        if ("USER".equals(readerType)) {
            conversation.setUnreadCountUser(0);
        } else {
            conversation.setUnreadCountSeller(0);
        }
        conversationRepository.save(conversation);
        
        // Also mark messages as read in DB if needed
        messageRepository.markAllAsRead(conversationId, readerId);
    }

    public Long getUserUnreadCount(Long userId) {
        return conversationRepository.countUnreadByUserId(userId);
    }

    public Long getSellerUnreadCount(Long sellerId) {
        return conversationRepository.countUnreadBySellerId(sellerId);
    }

    public String getSenderName(Long senderId, String senderType) {
        if ("USER".equals(senderType)) {
            return "User " + senderId; 
        } else {
            try {
                Seller seller = sellerService.getSellerById(senderId);
                return seller.getSellerName();
            } catch (Exception e) {
                return "Seller " + senderId;
            }
        }
    }
}
