package com.furniture.response;

import java.time.LocalDateTime;

import com.furniture.modal.Conversation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConversationResponse {
    private Long id;
    private Long userId;
    private String userName;
    private Long sellerId;
    private String sellerName;
    private Long orderId;
    private String orderDetails;
    
    private Long productId;
    private String productTitle;
    private String productImage;
    private String categoryId;
    private String parentCategoryId;
    
    private String lastMessagePreview;
    private String lastMessageSenderType;
    private LocalDateTime lastMessageAt;
    private Integer unreadCountUser;
    private Integer unreadCountSeller;

    public ConversationResponse(Conversation conversation) {
        this.id = conversation.getId();
        this.userId = conversation.getUser().getId();
        this.userName = conversation.getUser().getFullName();
        this.sellerId = conversation.getSeller().getId();
        this.sellerName = conversation.getSeller().getSellerName();
        
        if (conversation.getOrder() != null) {
            this.orderId = conversation.getOrder().getId();
            // this.orderDetails = ...; 
        }
        
        if (conversation.getProduct() != null) {
            this.productId = conversation.getProduct().getId();
            this.productTitle = conversation.getProduct().getTitle();
            if (conversation.getProduct().getImages() != null && !conversation.getProduct().getImages().isEmpty()) {
                this.productImage = conversation.getProduct().getImages().getFirst();
            }
            if (conversation.getProduct().getCategory() != null) {
                this.categoryId = conversation.getProduct().getCategory().getCategoryId();
                if (conversation.getProduct().getCategory().getParentCategory() != null) {
                    this.parentCategoryId = conversation.getProduct().getCategory().getParentCategory().getCategoryId();
                }
            }
        }
        
        this.lastMessageAt = conversation.getLastMessageAt();
        this.unreadCountUser = conversation.getUnreadCountUser();
        this.unreadCountSeller = conversation.getUnreadCountSeller();
        
        // Determine preview based on last message or default
        this.lastMessagePreview = "Bắt đầu trò chuyện";
    }

    public static ConversationResponse fromConversation(Conversation conversation, String lastMessagePreview, String lastMessageSenderType) {
        return ConversationResponse.builder()
                .id(conversation.getId())
                .userId(conversation.getUser().getId())
                .userName(conversation.getUser().getFullName())
                .sellerId(conversation.getSeller().getId())
                .sellerName(conversation.getSeller().getSellerName())
                .orderId(conversation.getOrder() != null ? conversation.getOrder().getId() : null)
                .orderDetails(conversation.getOrder() != null ? conversation.getOrder().getId().toString() : null)
                .productId(conversation.getProduct() != null ? conversation.getProduct().getId() : null)
                .productTitle(conversation.getProduct() != null ? conversation.getProduct().getTitle() : null)
                .productImage(conversation.getProduct() != null && !conversation.getProduct().getImages().isEmpty() 
                    ? conversation.getProduct().getImages().getFirst() : null)
                .categoryId(conversation.getProduct() != null && conversation.getProduct().getCategory() != null 
                    ? conversation.getProduct().getCategory().getCategoryId() : null)
                .parentCategoryId(conversation.getProduct() != null && conversation.getProduct().getCategory() != null && conversation.getProduct().getCategory().getParentCategory() != null
                    ? conversation.getProduct().getCategory().getParentCategory().getCategoryId() : null)
                .lastMessagePreview(lastMessagePreview)
                .lastMessageSenderType(lastMessageSenderType)
                .lastMessageAt(conversation.getLastMessageAt())
                .unreadCountUser(conversation.getUnreadCountUser())
                .unreadCountSeller(conversation.getUnreadCountSeller())
                .build();
    }
}
